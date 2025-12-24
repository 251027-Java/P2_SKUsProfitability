package com.project2.SKUProfitability.Repository;

import com.project2.SKUProfitability.Model.SKU;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SKURepository extends JpaRepository<SKU, Long> {
    
    // Find all SKUs for a specific user
    List<SKU> findByUserId(Long userId);
    
    // Find SKU by user ID and SKU identifier
    Optional<SKU> findByUserIdAndSku(Long userId, String sku);
    
    // Find SKU by user ID and ASIN
    Optional<SKU> findByUserIdAndAsin(Long userId, String asin);
    
    // Check if SKU exists for user
    boolean existsByUserIdAndSku(Long userId, String sku);
}

