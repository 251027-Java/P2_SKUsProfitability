package com.p2.product_service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.p2.product_service.dto.SKUDTO;
import com.p2.product_service.exception.ResourceNotFoundException;
import com.p2.product_service.model.SKU;
import com.p2.product_service.model.request.BrightData.BrightDataCollectByUrlRequest;
import com.p2.product_service.model.request.BrightData.BrightDataProductDetailsRequest;
import com.p2.product_service.model.response.DataBrightResponse;
import com.p2.product_service.repository.SKURepository;
import com.p2.product_service.util.DataTransformUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;

@Service
public class SKUService {

    private final SKURepository skuRepository;
    private final BasicCalculationService calculationService;
    private final HttpClient httpClient;

    public SKUService(SKURepository skuRepository, BasicCalculationService calculationService, HttpClient httpClient) {
        this.skuRepository = skuRepository;
        this.calculationService = calculationService;
        this.httpClient = httpClient;
    }

    @Value("${brightdata.api.url}")
    private String brightDataApiUrl;

    @Value("${brightdata.api.key}")
    private String apiKey;

    public List<SKU> getSKUs() {
//        List<BrightDataProductDetailsRequest> brightDataProductDetailRequests = new ArrayList<>();
//        urls.forEach(url -> {
//            BrightDataProductDetailsRequest productDetail = new BrightDataProductDetailsRequest();
//            productDetail.setUrl(url);
//            brightDataProductDetailRequests.add(productDetail);
//        });
//
//        BrightDataCollectByUrlRequest brightDataCollectByUrlRequest = new BrightDataCollectByUrlRequest();
//        brightDataCollectByUrlRequest.setInput(brightDataProductDetailRequests);
//
//        ObjectMapper mapper = new ObjectMapper();
//
//        String requestBody = mapper.writeValueAsString(brightDataCollectByUrlRequest);
//
//        HttpRequest request = HttpRequest.newBuilder()
//                .uri(new URI(brightDataApiUrl))
//                .header("Authorization", "Bearer " + apiKey)
//                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
//                .build();
//
//        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
//        DataBrightResponse dbResponse = mapper.readValue(response.body(), DataBrightResponse.class);
//
//        SKU sku = new SKU();
//
//        sku.setProductName(dbResponse.getTitle());
//        sku.setSellingPrice(BigDecimal.valueOf(dbResponse.getFinalPrice()));
//        sku.setDescription(dbResponse.getDescription());
//        sku.setImageUrl(dbResponse.getImageUrl());
//        sku.setWeight(BigDecimal.valueOf(dbResponse.getItemWeight()));
        //
        //
        // return new SKU();
        return skuRepository.findAll();
    }

    // public SKUDTO createSKU(SKUCreateDTO dto) {
    // ValidationUtil.validateSKUFormat(dto.sku());
    // ValidationUtil.validateProductName(dto.productName());
    // ValidationUtil.validateRequired(dto.description(), "Description");
    // ValidationUtil.validateCategory(dto.category());
    // ValidationUtil.validateDimension(dto.length(), "Length");
    // ValidationUtil.validateDimension(dto.width(), "Width");
    // ValidationUtil.validateDimension(dto.height(), "Height");
    // ValidationUtil.validateWeight(dto.weight());
    // ValidationUtil.validatePrice(dto.sellingPrice(), "Selling Price");
    //
    // if (skuRepository.existsBySku(dto.sku())) {
    // throw new IllegalArgumentException("SKU already exists: " + dto.sku());
    // }
    //
    // SKU sku = new SKU(
    // dto.sku(),
    // dto.productName(),
    // dto.description(),
    // dto.length(),
    // dto.width(),
    // dto.height(),
    // dto.weight(),
    // dto.category(),
    // dto.sellingPrice()
    // );
    //
    // calculateAndSetFees(sku);
    //
    // try {
    // SKU savedSku = skuRepository.save(sku);
    // return DataTransformUtil.toSKUDTO(savedSku);
    // } catch (DataIntegrityViolationException e) {
    // throw new IllegalArgumentException("SKU already exists: " + dto.sku());
    // }
    // }

    private void calculateAndSetFees(SKU sku) {
        if (sku.getLength() != null && sku.getWidth() != null &&
                sku.getHeight() != null && sku.getWeight() != null) {
            String sizeClassification = calculationService.determineSizeTier(
                    sku.getLength(), sku.getWidth(), sku.getHeight(), sku.getWeight());
            sku.setSizeClassification(sizeClassification);
        } else {
            sku.setSizeClassification(null);
        }

        BigDecimal fbaFee = calculationService.calculateFBAFulfillmentFee(
                sku.getLength(), sku.getWidth(), sku.getHeight(), sku.getWeight());
        sku.setFbaFulfillmentFee(fbaFee);

        BigDecimal referralFee = calculationService.calculateReferralFee(
                sku.getSellingPrice(), sku.getCategory());
        sku.setReferralFee(referralFee);

        BigDecimal storageFee = calculationService.calculateStorageFee(
                sku.getLength(), sku.getWidth(), sku.getHeight());
        sku.setStorageFee(storageFee);

        BigDecimal totalFees = calculationService.calculateTotalFees(
                fbaFee, referralFee, storageFee);
        sku.setTotalFees(totalFees);

        BigDecimal netProfit = calculationService.calculateNetProfit(
                sku.getSellingPrice(), totalFees);
        sku.setNetProfit(netProfit);
    }

    public List<SKUDTO> getAllSKUs() {
        // skuRepository.findAll() returns every product in the database
        return DataTransformUtil.toSKUDTOList(skuRepository.findAll());
    }

    public Optional<SKUDTO> getSKUById(Long skuId) {
        return skuRepository.findById(skuId)
                .map(DataTransformUtil::toSKUDTO);
    }

    public Optional<SKUDTO> searchBySku(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return Optional.empty();
        }

        String trimmedSearch = DataTransformUtil.normalizeString(searchTerm);
        Optional<SKU> skuBySku = skuRepository.findBySku(trimmedSearch);
        return skuBySku.map(DataTransformUtil::toSKUDTO);
    }

    public void deleteSKU(Long skuId) {
        SKU sku = skuRepository.findById(skuId)
                .orElseThrow(() -> new ResourceNotFoundException("SKU", skuId));

        skuRepository.delete(sku);
    }
}
