package com.p2.product_service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "seller_list_items")
@Data
@NoArgsConstructor
public class SellerListItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long listItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "list_id", nullable = false)
    private SellerList sellerList;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", nullable = false)
    private SKU sku;

    @Column(nullable = false, updatable = false)
    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        addedAt = LocalDateTime.now();
    }

    public SellerListItem(SellerList sellerList, SKU sku) {
        this.sellerList = sellerList;
        this.sku = sku;
    }
}
