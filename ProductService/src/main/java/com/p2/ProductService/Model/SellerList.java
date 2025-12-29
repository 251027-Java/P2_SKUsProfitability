package com.p2.ProductService.Model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Entity
@Table(name = "seller_lists")
@Data
@NoArgsConstructor
public class SellerList {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long listId;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 500)
    private String description;

    @OneToMany(mappedBy = "sellerList", cascade = CascadeType.ALL, orphanRemoval = true)
    private java.util.List<SellerListItem> items = new ArrayList<>();

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

    public SellerList(Long userId, String name, String description) {
        this.userId = userId;
        this.name = name;
        this.description = description;
    }
}
