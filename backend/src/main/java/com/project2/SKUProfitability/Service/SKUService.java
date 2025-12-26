package com.project2.SKUProfitability.Service;

import com.project2.SKUProfitability.DTO.SKUCreateDTO;
import com.project2.SKUProfitability.DTO.SKUDTO;
import com.project2.SKUProfitability.Exception.ResourceNotFoundException;
import com.project2.SKUProfitability.Model.SKU;
import com.project2.SKUProfitability.Repository.SKURepository;
import com.project2.SKUProfitability.Util.DataTransformUtil;
import com.project2.SKUProfitability.Util.ValidationUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class SKUService {
    
    private final SKURepository skuRepository;
    private final FBAFeeCalculatorService feeCalculatorService;
    
    public SKUService(SKURepository skuRepository, FBAFeeCalculatorService feeCalculatorService) {
        this.skuRepository = skuRepository;
        this.feeCalculatorService = feeCalculatorService;
    }
    
    public SKUDTO createSKU(Long userId, SKUCreateDTO dto) {
        ValidationUtil.validateSKUFormat(dto.sku());
        ValidationUtil.validateProductName(dto.productName());
        ValidationUtil.validateRequired(dto.description(), "Description");
        ValidationUtil.validateCategory(dto.category());
        ValidationUtil.validateDimension(dto.length(), "Length");
        ValidationUtil.validateDimension(dto.width(), "Width");
        ValidationUtil.validateDimension(dto.height(), "Height");
        ValidationUtil.validateWeight(dto.weight());
        ValidationUtil.validatePrice(dto.sellingPrice(), "Selling Price");
        
        if (skuRepository.existsBySku(dto.sku())) {
            throw new IllegalArgumentException("SKU already exists: " + dto.sku());
        }
        
        SKU sku = new SKU(
                userId,
                dto.sku(),
                dto.productName(),
                dto.description(),
                dto.length(),
                dto.width(),
                dto.height(),
                dto.weight(),
                dto.category(),
                dto.sellingPrice()
        );
        
        calculateAndSetFees(sku);
        
        try {
            SKU savedSku = skuRepository.save(sku);
            return DataTransformUtil.toSKUDTO(savedSku);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("SKU already exists: " + dto.sku());
        }
    }
    
    public List<SKUDTO> importFromCSV(Long userId, MultipartFile file) {
        List<SKUDTO> importedSKUs = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        try (Reader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            
            CSVParser csvParser = new CSVParser(reader, CSVFormat.DEFAULT
                    .withFirstRecordAsHeader()
                    .withIgnoreHeaderCase()
                    .withTrim());
            
            for (CSVRecord record : csvParser) {
                try {
                    SKUCreateDTO dto = parseCSVRecord(record);
                    SKUDTO skuDTO = createSKU(userId, dto);
                    importedSKUs.add(skuDTO);
                } catch (Exception e) {
                    errors.add("Row " + record.getRecordNumber() + ": " + e.getMessage());
                }
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage(), e);
        }
        
        if (!errors.isEmpty() && importedSKUs.isEmpty()) {
            throw new RuntimeException("Failed to import any SKUs. Errors: " + String.join(", ", errors));
        }
        
        return importedSKUs;
    }
    
    private SKUCreateDTO parseCSVRecord(CSVRecord record) {
        String sku = getRequiredField(record, "SKU");
        String productName = getRequiredField(record, "Product Name");
        String description = getRequiredField(record, "Description");
        BigDecimal length = parseDecimal(record, "Length");
        BigDecimal width = parseDecimal(record, "Width");
        BigDecimal height = parseDecimal(record, "Height");
        BigDecimal weight = parseDecimal(record, "Weight");
        String category = getRequiredField(record, "Category");
        BigDecimal sellingPrice = parseDecimal(record, "Selling Price");
        
        return new SKUCreateDTO(
                sku, productName, description, length, width, height, 
                weight, category, sellingPrice
        );
    }
    
    private String getRequiredField(CSVRecord record, String header) {
        String value = record.get(header);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Required field missing: " + header);
        }
        return value.trim();
    }
    
    private BigDecimal parseDecimal(CSVRecord record, String header) {
        try {
            String value = record.get(header);
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            value = value.replace("$", "").replace(",", "").trim();
            return DataTransformUtil.parseBigDecimal(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid number format for " + header + ": " + e.getMessage());
        }
    }
    
    private void calculateAndSetFees(SKU sku) {
        if (sku.getLength() != null && sku.getWidth() != null && 
            sku.getHeight() != null && sku.getWeight() != null) {
            String sizeClassification = feeCalculatorService.determineSizeTier(
                    sku.getLength(), sku.getWidth(), sku.getHeight(), sku.getWeight()
            );
            sku.setSizeClassification(sizeClassification);
        } else {
            sku.setSizeClassification(null);
        }
        
        BigDecimal fbaFee = feeCalculatorService.calculateFBAFulfillmentFee(
                sku.getLength(), sku.getWidth(), sku.getHeight(), sku.getWeight()
        );
        sku.setFbaFulfillmentFee(fbaFee);
        
        BigDecimal referralFee = feeCalculatorService.calculateReferralFee(
                sku.getSellingPrice(), sku.getCategory()
        );
        sku.setReferralFee(referralFee);
        
        BigDecimal storageFee = feeCalculatorService.calculateStorageFee(
                sku.getLength(), sku.getWidth(), sku.getHeight()
        );
        sku.setStorageFee(storageFee);
        
        BigDecimal totalFees = feeCalculatorService.calculateTotalFees(
                fbaFee, referralFee, storageFee
        );
        sku.setTotalFees(totalFees);
        
        BigDecimal netProfit = feeCalculatorService.calculateNetProfit(
                sku.getSellingPrice(), totalFees
        );
        sku.setNetProfit(netProfit);
    }
    
    public List<SKUDTO> getAllSKUsByUserId(Long userId) {
        return DataTransformUtil.toSKUDTOList(skuRepository.findByUserId(userId));
    }
    
    public Optional<SKUDTO> getSKUById(Long skuId, Long userId) {
        return skuRepository.findById(skuId)
                .filter(sku -> sku.getUserId().equals(userId))
                .map(DataTransformUtil::toSKUDTO);
    }
    
    public Optional<SKUDTO> searchBySku(Long userId, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return Optional.empty();
        }
        
        String trimmedSearch = DataTransformUtil.normalizeString(searchTerm);
        Optional<SKU> skuBySku = skuRepository.findByUserIdAndSku(userId, trimmedSearch);
        return skuBySku.map(DataTransformUtil::toSKUDTO);
    }
    
    public void deleteSKU(Long skuId, Long userId) {
        SKU sku = skuRepository.findById(skuId)
                .filter(s -> s.getUserId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("SKU", skuId));
        
        skuRepository.delete(sku);
    }
}

