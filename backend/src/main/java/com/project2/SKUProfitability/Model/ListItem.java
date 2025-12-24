package com.project2.SKUProfitability.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "list_items")
@Data
@NoArgsConstructor
public class ListItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long listItemId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "list_id", nullable = false)
    private List list;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sku_id", nullable = false)
    private SKU sku;
    
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
    
    public ListItem(List list, SKU sku) {
        this.list = list;
        this.sku = sku;
    }
}

