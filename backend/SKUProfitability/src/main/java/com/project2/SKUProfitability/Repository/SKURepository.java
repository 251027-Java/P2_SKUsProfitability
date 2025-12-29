package com.project2.SKUProfitability.Repository;

import com.project2.SKUProfitability.Model.SKU;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SKURepository extends JpaRepository<SKU, Long> {
    
    List<SKU> findByUserId(Long userId);
    
    Optional<SKU> findByUserIdAndSku(Long userId, String sku);
    
    boolean existsByUserIdAndSku(Long userId, String sku);
    
    boolean existsBySku(String sku);
}

