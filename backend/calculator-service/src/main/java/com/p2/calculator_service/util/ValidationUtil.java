package com.p2.calculator_service.util;

import java.math.BigDecimal;

public class ValidationUtil {
    
    private static final BigDecimal MIN_DIMENSION = BigDecimal.ZERO;
    private static final BigDecimal MAX_DIMENSION = new BigDecimal("1000");
    private static final BigDecimal MIN_WEIGHT = BigDecimal.ZERO;
    private static final BigDecimal MAX_WEIGHT = new BigDecimal("10000");
    private static final BigDecimal MIN_PRICE = BigDecimal.ZERO;
    private static final BigDecimal MAX_PRICE = new BigDecimal("1000000");
    
    public static void validateDimension(BigDecimal dimension, String dimensionName) {
        if (dimension != null) {
            if (dimension.compareTo(MIN_DIMENSION) < 0) {
                throw new IllegalArgumentException(dimensionName + " cannot be negative");
            }
            if (dimension.compareTo(MAX_DIMENSION) > 0) {
                throw new IllegalArgumentException(dimensionName + " cannot exceed " + MAX_DIMENSION + " inches");
            }
        }
    }
    
    public static void validateWeight(BigDecimal weight) {
        if (weight != null) {
            if (weight.compareTo(MIN_WEIGHT) < 0) {
                throw new IllegalArgumentException("Weight cannot be negative");
            }
            if (weight.compareTo(MAX_WEIGHT) > 0) {
                throw new IllegalArgumentException("Weight cannot exceed " + MAX_WEIGHT + " pounds");
            }
        }
    }
    
    public static void validatePrice(BigDecimal price, String priceName) {
        if (price != null) {
            if (price.compareTo(MIN_PRICE) < 0) {
                throw new IllegalArgumentException(priceName + " cannot be negative");
            }
            if (price.compareTo(MAX_PRICE) > 0) {
                throw new IllegalArgumentException(priceName + " cannot exceed $" + MAX_PRICE);
            }
        }
    }
    
    public static void validateTimeInStorage(BigDecimal timeInStorage) {
        if (timeInStorage != null) {
            if (timeInStorage.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Time in storage cannot be negative");
            }
            if (timeInStorage.compareTo(new BigDecimal("120")) > 0) {
                throw new IllegalArgumentException("Time in storage cannot exceed 120 months");
            }
        }
    }
    
}
