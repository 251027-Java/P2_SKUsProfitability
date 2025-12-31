package com.p2.product_service.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class CalculationUtil {
    
    private static final int DEFAULT_SCALE = 2;
    private static final RoundingMode DEFAULT_ROUNDING_MODE = RoundingMode.HALF_UP;
    
    public static BigDecimal roundToTwoDecimals(BigDecimal value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        return value.setScale(DEFAULT_SCALE, DEFAULT_ROUNDING_MODE);
    }
    
    public static BigDecimal calculatePercentage(BigDecimal value, BigDecimal percentage) {
        if (value == null || percentage == null) {
            return BigDecimal.ZERO;
        }
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal percentageDecimal = percentage.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
        return roundToTwoDecimals(value.multiply(percentageDecimal));
    }
    
    public static BigDecimal calculateProfitMargin(BigDecimal netProfit, BigDecimal sellingPrice) {
        if (netProfit == null || sellingPrice == null || sellingPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        BigDecimal margin = netProfit.divide(sellingPrice, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
        return roundToTwoDecimals(margin);
    }
    
    public static BigDecimal calculateROI(BigDecimal netProfit, BigDecimal cost) {
        if (netProfit == null || cost == null || cost.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }
        BigDecimal roi = netProfit.divide(cost, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
        return roundToTwoDecimals(roi);
    }
    
    public static BigDecimal calculateDimensionalWeight(BigDecimal length, BigDecimal width, BigDecimal height) {
        if (length == null || width == null || height == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal volume = length.multiply(width).multiply(height);
        return roundToTwoDecimals(volume.divide(new BigDecimal("166"), 4, RoundingMode.HALF_UP));
    }
    
    public static BigDecimal calculateBillableWeight(BigDecimal actualWeight, BigDecimal dimensionalWeight) {
        if (actualWeight == null && dimensionalWeight == null) {
            return BigDecimal.ZERO;
        }
        if (actualWeight == null) {
            return dimensionalWeight;
        }
        if (dimensionalWeight == null) {
            return actualWeight;
        }
        return actualWeight.max(dimensionalWeight);
    }
    
    public static BigDecimal calculateCubicFeet(BigDecimal length, BigDecimal width, BigDecimal height) {
        if (length == null || width == null || height == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal cubicInches = length.multiply(width).multiply(height);
        BigDecimal cubicFeet = cubicInches.divide(new BigDecimal("1728"), 4, RoundingMode.HALF_UP);
        return roundToTwoDecimals(cubicFeet);
    }
    
    public static BigDecimal calculateNetProfit(BigDecimal sellingPrice, BigDecimal totalFees) {
        if (sellingPrice == null) return BigDecimal.ZERO;
        BigDecimal totalCost = totalFees != null ? totalFees : BigDecimal.ZERO;
        return roundToTwoDecimals(sellingPrice.subtract(totalCost));
    }
    
    public static BigDecimal safeAdd(BigDecimal a, BigDecimal b) {
        if (a == null && b == null) return BigDecimal.ZERO;
        if (a == null) return b;
        if (b == null) return a;
        return roundToTwoDecimals(a.add(b));
    }
    
}

