package com.project2.SKUProfitability.DTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record SKUDTO(
        Long skuId,
        Long userId,
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
        BigDecimal cost,
        BigDecimal targetROI,
        BigDecimal fbaFulfillmentFee,
        BigDecimal referralFee,
        BigDecimal storageFee,
        BigDecimal totalFees,
        BigDecimal netProfit,
        BigDecimal maxCost,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

