package com.p2.CalculatorService.repository;

import com.p2.CalculatorService.model.Calculation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CalculationRepository extends JpaRepository<Calculation, Long> {
    
    List<Calculation> findByUserId(Long userId);
    
    List<Calculation> findByUserIdOrderByCreatedAtDesc(Long userId);
}
