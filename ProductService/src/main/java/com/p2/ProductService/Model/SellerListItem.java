package com.p2.ProductService.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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
    private SellerList sellerlist;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", nullable = false)
    private SKU sku;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    public SellerListItem(SellerList sellerlist, SKU sku) {
        this.sellerlist = sellerlist;
        this.sku = sku;
    }
}
