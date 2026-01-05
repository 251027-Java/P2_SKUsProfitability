package com.p2.calculator_service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "calculations")
@Data
@NoArgsConstructor
public class Calculation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "userId")
    private Long userId;

    @Column(length = 100)
    private String sku;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal length;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal width;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal height;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal weight;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal sellingPrice;
    
    @Column(length = 50)
    private String category;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal timeInStorage;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal freightCost;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal freightCostUnit;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal otherCosts;
    
    @Column(length = 20)
    private String otherCostsType;
    
    @Column(length = 50)
    private String fbaFeeCategory;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal referralFeePercentage;
    
    @Column(length = 50)
    private String sizeTier;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal fbaFulfillmentFee;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal referralFee;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal storageFeeJanSep;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal storageFeeOctDec;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal unitFreightCost;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal totalFeesJanSep;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal totalFeesOctDec;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal netProfitJanSep;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal netProfitOctDec;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal profitMarginJanSep;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal profitMarginOctDec;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal roiJanSep;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal roiOctDec;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
