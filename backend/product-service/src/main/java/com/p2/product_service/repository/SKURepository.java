package com.p2.product_service.repository;

import com.p2.product_service.model.SKU;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SKURepository extends JpaRepository<SKU, Long> {

    Optional<SKU> findBySku(String sku);

    boolean existsBySku(String sku);
}
