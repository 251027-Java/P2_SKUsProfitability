package com.p2.product_service.repository;

import com.p2.product_service.model.SellerList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerListRepository extends JpaRepository<SellerList, Long> {
    java.util.List<SellerList> findByUserId(Long userId);
    boolean existsByUserIdAndName(Long userId, String name);
}
