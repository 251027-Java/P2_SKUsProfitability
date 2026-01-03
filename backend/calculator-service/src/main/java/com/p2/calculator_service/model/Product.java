package com.p2.calculator_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "skus")
@Data
@NoArgsConstructor
public class Product {

    @Id
    @Column(name = "sku_id")
    private Long id;

    @Column(name = "sku", unique = true)
    private String sku;

    @Column(name = "product_name")
    private String name;

    private BigDecimal length;
    private BigDecimal width;
    private BigDecimal height;
    private BigDecimal weight;

    @Column(name = "selling_price")
    private BigDecimal sellingPrice;

    private String category;
}
