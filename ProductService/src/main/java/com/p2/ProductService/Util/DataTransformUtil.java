package com.p2.ProductService.Util;

import com.p2.ProductService.DTO.SKUDTO;
import com.p2.ProductService.Model.SKU;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class DataTransformUtil {
    
    public static SKUDTO toSKUDTO(SKU sku) {
        if (sku == null) return null;
        return new SKUDTO(
            sku.getSkuId(),
            sku.getSku(),
            sku.getProductName(),
            sku.getDescription(),
            sku.getLength(),
            sku.getWidth(),
            sku.getHeight(),
            sku.getWeight(),
            sku.getCategory(),
            sku.getSizeClassification(),
            sku.getSellingPrice(),
            sku.getFbaFulfillmentFee(),
            sku.getReferralFee(),
            sku.getStorageFee(),
            sku.getTotalFees(),
            sku.getNetProfit(),
            sku.getCreatedAt(),
            sku.getUpdatedAt()
        );
    }
    
    public static List<SKUDTO> toSKUDTOList(List<SKU> skus) {
        if (skus == null) return List.of();
        return skus.stream().map(DataTransformUtil::toSKUDTO).collect(Collectors.toList());
    }
    
    public static BigDecimal parseBigDecimal(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try {
            return new BigDecimal(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    public static String normalizeString(String value) {
        if (value == null) return null;
        return value.trim().replaceAll("\\s+", " ");
    }
    
}

