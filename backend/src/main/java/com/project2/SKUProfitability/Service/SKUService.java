package com.project2.SKUProfitability.Service;

import com.project2.SKUProfitability.DTO.SKUCreateDTO;
import com.project2.SKUProfitability.DTO.SKUDTO;
import com.project2.SKUProfitability.Model.SKU;
import com.project2.SKUProfitability.Repository.SKURepository;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
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
        if (skuRepository.existsByUserIdAndSku(userId, dto.sku())) {
            throw new IllegalArgumentException("SKU already exists: " + dto.sku());
        }
        
        SKU sku = new SKU(
                userId,
                dto.sku(),
                dto.asin(),
                dto.productName(),
                dto.length(),
                dto.width(),
                dto.height(),
                dto.weight(),
                dto.category(),
                dto.sellingPrice(),
                dto.cost(),
                dto.targetROI()
        );
        
        calculateAndSetFees(sku);
        
        SKU savedSku = skuRepository.save(sku);
        return convertToDTO(savedSku);
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
        String asin = getOptionalField(record, "ASIN");
        String productName = getOptionalField(record, "Product Name");
        BigDecimal length = parseDecimal(record, "Length");
        BigDecimal width = parseDecimal(record, "Width");
        BigDecimal height = parseDecimal(record, "Height");
        BigDecimal weight = parseDecimal(record, "Weight");
        String category = getOptionalField(record, "Category");
        BigDecimal sellingPrice = parseDecimal(record, "Selling Price");
        BigDecimal cost = parseDecimal(record, "Cost");
        BigDecimal targetROI = parseDecimal(record, "Target ROI");
        
        return new SKUCreateDTO(
                sku, asin, productName, length, width, height, 
                weight, category, sellingPrice, cost, targetROI
        );
    }
    
    private String getRequiredField(CSVRecord record, String header) {
        try {
            String value = record.get(header);
            if (value == null || value.trim().isEmpty()) {
                throw new IllegalArgumentException("Required field missing: " + header);
            }
            return value.trim();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Required field missing: " + header);
        }
    }
    
    private String getOptionalField(CSVRecord record, String header) {
        try {
            return record.get(header);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    private BigDecimal parseDecimal(CSVRecord record, String header) {
        try {
            String value = record.get(header);
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            // Remove any currency symbols or commas
            value = value.replace("$", "").replace(",", "").trim();
            return new BigDecimal(value);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid number format for " + header + ": " + e.getMessage());
        }
    }
    
    private void calculateAndSetFees(SKU sku) {
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
                sku.getSellingPrice(), sku.getCost(), totalFees
        );
        sku.setNetProfit(netProfit);
        
        BigDecimal profitMargin = feeCalculatorService.calculateProfitMargin(
                sku.getSellingPrice(), netProfit
        );
        sku.setProfitMargin(profitMargin);
        
        if (sku.getCost() != null && sku.getCost().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal roi = feeCalculatorService.calculateROI(sku.getCost(), netProfit);
            sku.setRoi(roi);
        }
        
        if (sku.getTargetROI() != null && sku.getTargetROI().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal maxCost = feeCalculatorService.calculateMaxCostForROI(
                    sku.getSellingPrice(), totalFees, sku.getTargetROI()
            );
            sku.setMaxCost(maxCost);
        }
    }
    
    public List<SKUDTO> getAllSKUsByUserId(Long userId) {
        return skuRepository.findByUserId(userId).stream()
                .map(this::convertToDTO)
                .toList();
    }
    
    public Optional<SKUDTO> getSKUById(Long skuId, Long userId) {
        return skuRepository.findById(skuId)
                .filter(sku -> sku.getUserId().equals(userId))
                .map(this::convertToDTO);
    }
    
    public Optional<SKUDTO> searchBySkuOrAsin(Long userId, String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return Optional.empty();
        }
        
        String trimmedSearch = searchTerm.trim();
        
        Optional<SKU> skuBySku = skuRepository.findByUserIdAndSku(userId, trimmedSearch);
        if (skuBySku.isPresent()) {
            return skuBySku.map(this::convertToDTO);
        }
        
        Optional<SKU> skuByAsin = skuRepository.findByUserIdAndAsin(userId, trimmedSearch);
        return skuByAsin.map(this::convertToDTO);
    }
    
    public SKUDTO updateSKU(Long skuId, Long userId, SKUCreateDTO dto) {
        SKU sku = skuRepository.findById(skuId)
                .filter(s -> s.getUserId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("SKU not found"));
        
        sku.setSku(dto.sku());
        sku.setAsin(dto.asin());
        sku.setProductName(dto.productName());
        sku.setLength(dto.length());
        sku.setWidth(dto.width());
        sku.setHeight(dto.height());
        sku.setWeight(dto.weight());
        sku.setCategory(dto.category());
        sku.setSellingPrice(dto.sellingPrice());
        sku.setCost(dto.cost());
        sku.setTargetROI(dto.targetROI());
        
        calculateAndSetFees(sku);
        
        SKU updatedSku = skuRepository.save(sku);
        return convertToDTO(updatedSku);
    }
    
    public void deleteSKU(Long skuId, Long userId) {
        SKU sku = skuRepository.findById(skuId)
                .filter(s -> s.getUserId().equals(userId))
                .orElseThrow(() -> new IllegalArgumentException("SKU not found"));
        
        skuRepository.delete(sku);
    }
    
    public SKUDTO convertToDTO(SKU sku) {
        return new SKUDTO(
                sku.getSkuId(),
                sku.getUserId(),
                sku.getSku(),
                sku.getAsin(),
                sku.getProductName(),
                sku.getLength(),
                sku.getWidth(),
                sku.getHeight(),
                sku.getWeight(),
                sku.getCategory(),
                sku.getSellingPrice(),
                sku.getCost(),
                sku.getTargetROI(),
                sku.getFbaFulfillmentFee(),
                sku.getReferralFee(),
                sku.getStorageFee(),
                sku.getTotalFees(),
                sku.getNetProfit(),
                sku.getProfitMargin(),
                sku.getRoi(),
                sku.getMaxCost(),
                sku.getCreatedAt(),
                sku.getUpdatedAt()
        );
    }
}

