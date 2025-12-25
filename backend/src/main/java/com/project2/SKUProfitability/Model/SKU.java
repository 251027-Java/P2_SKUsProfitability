package com.project2.SKUProfitability.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "skus")
@Data
@NoArgsConstructor
public class SKU {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long skuId;
    
    @Column(nullable = false)
    private Long userId; // Foreign key to AppUser
    
    @Column(nullable = false, length = 50)
    private String sku; // Seller's SKU identifier
    
    @Column(length = 200)
    private String productName;
    
    @Column(columnDefinition = "TEXT")
    private String description; // Product description from CSV
    
    // Dimensions in inches
    @Column(precision = 10, scale = 2)
    private BigDecimal length;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal width;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal height;
    
    // Weight in pounds
    @Column(precision = 10, scale = 2)
    private BigDecimal weight;
    
    @Column(length = 50)
    private String category; // Product category
    
    @Column(length = 50)
    private String sizeClassification; // Size tier: Small Standard, Large Standard, Small Oversize, Large Oversize
    
    // Pricing
    @Column(precision = 10, scale = 2)
    private BigDecimal sellingPrice;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal cost; // Product cost
    
    @Column(precision = 10, scale = 2)
    private BigDecimal targetROI; // Target ROI percentage
    
    // Amazon Fees (calculated)
    @Column(precision = 10, scale = 2)
    private BigDecimal fbaFulfillmentFee;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal referralFee;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal storageFee; // Monthly storage fee
    
    @Column(precision = 10, scale = 2)
    private BigDecimal totalFees;
    
    // Profitability
    @Column(precision = 10, scale = 2)
    private BigDecimal netProfit;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal maxCost; // Maximum cost to achieve target ROI
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    public SKU(Long userId, String sku, String productName, String description,
               BigDecimal length, BigDecimal width, BigDecimal height, 
               BigDecimal weight, String category, BigDecimal sellingPrice, BigDecimal cost, BigDecimal targetROI) {
        this.userId = userId;
        this.sku = sku;
        this.productName = productName;
        this.description = description;
        this.length = length;
        this.width = width;
        this.height = height;
        this.weight = weight;
        this.category = category;
        this.sellingPrice = sellingPrice;
        this.cost = cost;
        this.targetROI = targetROI;
    }
}

