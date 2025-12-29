package com.p2.ProductService.Service;


import com.p2.ProductService.DTO.SKUCreateDTO;
import com.p2.ProductService.DTO.SKUDTO;
import com.p2.ProductService.Exception.ResourceNotFoundException;
import com.p2.ProductService.Model.SKU;
import com.p2.ProductService.Repository.SKURepository;
import com.p2.ProductService.Util.DataTransformUtil;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.p2.ProductService.Util.ValidationUtil;

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
    private final BasicCalculationService calculationService;

    public SKUService(SKURepository skuRepository, BasicCalculationService calculationService) {
        this.skuRepository = skuRepository;
        this.calculationService = calculationService;
    }

    public SKUDTO createSKU(SKUCreateDTO dto) {
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
                    SKUDTO skuDTO = createSKU(dto);
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
            String sizeClassification = calculationService.determineSizeTier(
                    sku.getLength(), sku.getWidth(), sku.getHeight(), sku.getWeight()
            );
            sku.setSizeClassification(sizeClassification);
        } else {
            sku.setSizeClassification(null);
        }

        BigDecimal fbaFee = calculationService.calculateFBAFulfillmentFee(
                sku.getLength(), sku.getWidth(), sku.getHeight(), sku.getWeight()
        );
        sku.setFbaFulfillmentFee(fbaFee);

        BigDecimal referralFee = calculationService.calculateReferralFee(
                sku.getSellingPrice(), sku.getCategory()
        );
        sku.setReferralFee(referralFee);

        BigDecimal storageFee = calculationService.calculateStorageFee(
                sku.getLength(), sku.getWidth(), sku.getHeight()
        );
        sku.setStorageFee(storageFee);

        BigDecimal totalFees = calculationService.calculateTotalFees(
                fbaFee, referralFee, storageFee
        );
        sku.setTotalFees(totalFees);

        BigDecimal netProfit = calculationService.calculateNetProfit(
                sku.getSellingPrice(), totalFees
        );
        sku.setNetProfit(netProfit);
    }

    public List<SKUDTO> getAllSKUsByUserId(Long userId) {
        return DataTransformUtil.toSKUDTOList(skuRepository.findByUserId(userId));
    }

    public Optional<SKUDTO> getSKUById(Long skuId) {
        return skuRepository.findById(skuId)
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

    public void deleteSKU(Long skuId) {
        SKU sku = skuRepository.findById(skuId)
                .orElseThrow(() -> new ResourceNotFoundException("SKU", skuId));

        skuRepository.delete(sku);
    }
}


