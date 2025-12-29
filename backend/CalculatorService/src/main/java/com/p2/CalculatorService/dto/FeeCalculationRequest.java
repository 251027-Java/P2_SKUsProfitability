package com.p2.CalculatorService.dto;

import java.math.BigDecimal;

public record FeeCalculationRequest(
        BigDecimal length,
        BigDecimal width,
        BigDecimal height,
        BigDecimal weight,
        String category,
        BigDecimal sellingPrice,
        BigDecimal timeInStorage,
        BigDecimal freightCost,
        BigDecimal freightCostUnit,
        BigDecimal otherCosts,
        String otherCostsType,
        String fbaFeeCategory,
        BigDecimal referralFeePercentage,
        Long userId
) {}
