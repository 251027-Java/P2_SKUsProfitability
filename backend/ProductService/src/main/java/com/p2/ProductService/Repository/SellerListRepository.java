package com.p2.ProductService.Repository;

import com.p2.ProductService.Model.SellerList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerListRepository extends JpaRepository<SellerList, Long> {
    java.util.List<SellerList> findByUserId(Long userId);
    boolean existsByUserIdAndName(Long userId, String name);
}
