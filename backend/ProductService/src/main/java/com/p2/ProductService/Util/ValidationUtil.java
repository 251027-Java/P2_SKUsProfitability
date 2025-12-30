package com.p2.ProductService.Util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public class ValidationUtil {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$"
    );
    
    private static final Pattern SKU_PATTERN = Pattern.compile("^[A-Za-z0-9_-]+$");
    
    private static final BigDecimal MIN_DIMENSION = BigDecimal.ZERO;
    private static final BigDecimal MAX_DIMENSION = new BigDecimal("1000");
    private static final BigDecimal MIN_WEIGHT = BigDecimal.ZERO;
    private static final BigDecimal MAX_WEIGHT = new BigDecimal("10000");
    private static final BigDecimal MIN_PRICE = BigDecimal.ZERO;
    private static final BigDecimal MAX_PRICE = new BigDecimal("1000000");
    
    public static void validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }
    
    public static void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }
    
    public static void validateSKUFormat(String sku) {
        if (sku == null || sku.trim().isEmpty()) {
            throw new IllegalArgumentException("SKU is required");
        }
        if (sku.length() > 50) {
            throw new IllegalArgumentException("SKU must be 50 characters or less");
        }
        if (!SKU_PATTERN.matcher(sku).matches()) {
            throw new IllegalArgumentException("SKU can only contain letters, numbers, underscores, and hyphens");
        }
    }
    
    public static void validateProductName(String productName) {
        validateRequired(productName, "Product Name");
        if (productName.length() > 200) {
            throw new IllegalArgumentException("Product Name must be 200 characters or less");
        }
    }
    
    public static void validateCategory(String category) {
        validateRequired(category, "Category");
        if (category.length() > 50) {
            throw new IllegalArgumentException("Category must be 50 characters or less");
        }
    }
    
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
    
    public static void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
        if (password.length() > 100) {
            throw new IllegalArgumentException("Password must be 100 characters or less");
        }
    }
    
    public static void validateName(String name, String fieldName) {
        validateRequired(name, fieldName);
        if (name.length() > 100) {
            throw new IllegalArgumentException(fieldName + " must be 100 characters or less");
        }
    }
    
}

