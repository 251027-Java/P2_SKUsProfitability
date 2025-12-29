package com.p2.ProductService.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SKUDTO(
        Long skuId,
        String sku,
        String productName,
        String description,
        BigDecimal length,
        BigDecimal width,
        BigDecimal height,
        BigDecimal weight,
        String category,
        String sizeClassification,
        BigDecimal sellingPrice,
        BigDecimal fbaFulfillmentFee,
        BigDecimal referralFee,
        BigDecimal storageFee,
        BigDecimal totalFees,
        BigDecimal netProfit,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
