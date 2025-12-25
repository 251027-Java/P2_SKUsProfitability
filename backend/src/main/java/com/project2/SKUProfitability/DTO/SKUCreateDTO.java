package com.project2.SKUProfitability.DTO;

import java.math.BigDecimal;

public record SKUCreateDTO(
        String sku,
        String productName,
        String description,
        BigDecimal length,
        BigDecimal width,
        BigDecimal height,
        BigDecimal weight,
        String category,
        BigDecimal sellingPrice,
        BigDecimal cost,
        BigDecimal targetROI
) {}

