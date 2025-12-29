package com.project2.SKUProfitability.Repository;

import com.project2.SKUProfitability.Model.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ListRepository extends JpaRepository<List, Long> {
    java.util.List<List> findByUserId(Long userId);
    boolean existsByUserIdAndName(Long userId, String name);
}

