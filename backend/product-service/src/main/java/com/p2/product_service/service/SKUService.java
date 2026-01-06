package com.p2.product_service.service;

import com.p2.product_service.dto.SKUDTO;
import com.p2.product_service.exception.ResourceNotFoundException;
import com.p2.product_service.mapper.DataBrightSkuToProductServiceSkuMapper;
import com.p2.product_service.model.SKU;
import com.p2.product_service.model.request.BrightData.BrightDataDiscoverByBestSellerRequest;
import com.p2.product_service.model.response.BrightDataCategoryResponse;
import com.p2.product_service.repository.SKURepository;
import com.p2.product_service.util.DataTransformUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SKUService {

    private final SKURepository skuRepository;
    private final BasicCalculationService calculationService;
    private final BrightDataService brightDataService;

    private final DataBrightSkuToProductServiceSkuMapper skuMapper;

    public List<SKU> addSkusByCategory(BrightDataDiscoverByBestSellerRequest request) throws RuntimeException {

           BrightDataCategoryResponse brightDataCategoryResponse = brightDataService.getBestSellersByCategory(request);

            List<SKU> skus = brightDataCategoryResponse
                                    .getSkuResponseList()
                                    .stream()
                                    .map(skuMapper::dataBrightSkuToProductServiceSku)
                                    .toList();

            return skuRepository.saveAll(skus);

    }

    public List<SKU> getSKUs() {
        return skuRepository.findAll();
    }

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
