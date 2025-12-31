package com.p2.product_service.model;

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

//    @Column(nullable = false)
//    private Long userId;

    @Column(nullable = false, unique = true, length = 50)
    private String sku;

    @Column(nullable = false, length = 200)
    private String productName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(precision = 10, scale = 2)
    private BigDecimal length;

    @Column(precision = 10, scale = 2)
    private BigDecimal width;

    @Column(precision = 10, scale = 2)
    private BigDecimal height;

    @Column(precision = 10, scale = 2)
    private BigDecimal weight;

    @Column(nullable = false, length = 50)
    private String category;

    @Column(length = 50)
    private String sizeClassification;

    @Column(precision = 10, scale = 2)
    private BigDecimal sellingPrice;

    @Column(precision = 10, scale = 2)
    private BigDecimal fbaFulfillmentFee;

    @Column(precision = 10, scale = 2)
    private BigDecimal referralFee;

    @Column(precision = 10, scale = 2)
    private BigDecimal storageFee;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalFees;

    @Column(precision = 10, scale = 2)
    private BigDecimal netProfit;

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

    public SKU(String sku, String productName, String description,
               BigDecimal length, BigDecimal width, BigDecimal height,
               BigDecimal weight, String category, BigDecimal sellingPrice) {
        this.sku = sku;
        this.productName = productName;
        this.description = description;
        this.length = length;
        this.width = width;
        this.height = height;
        this.weight = weight;
        this.category = category;
        this.sellingPrice = sellingPrice;
    }
}


