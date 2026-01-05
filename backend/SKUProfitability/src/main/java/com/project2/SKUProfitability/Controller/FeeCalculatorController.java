package com.project2.SKUProfitability.Controller;

import com.project2.SKUProfitability.Client.CalculatorServiceClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/calculator")
public class FeeCalculatorController {
    private final CalculatorServiceClient calculatorServiceClient;
    
    public FeeCalculatorController(CalculatorServiceClient calculatorServiceClient) {
        this.calculatorServiceClient = calculatorServiceClient;
    }
    
    @PostMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculateFees(@RequestBody FeeCalculationRequest request) {
        CalculatorServiceClient.FeeCalculationRequest microserviceRequest = 
            new CalculatorServiceClient.FeeCalculationRequest(
                request.userId(),
                request.length(),
                request.width(),
                request.height(),
                request.weight(),
                request.category(),
                request.sellingPrice(),
                request.timeInStorage(),
                request.freightCost(),
                request.freightCostUnit(),
                request.otherCosts(),
                request.otherCostsType(),
                request.fbaFeeCategory(),
                request.referralFeePercentage()
            );
        
        return calculatorServiceClient.calculateFees(microserviceRequest);
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> health() {
        boolean calculatorServiceHealthy = calculatorServiceClient.isServiceHealthy();
        
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "Main Backend",
            "calculatorServiceReachable", calculatorServiceHealthy
        ));
    }
    
    /**
     * Request DTO for main backend - includes userId for database tracking
     */
    public record FeeCalculationRequest(
            Long userId,
            BigDecimal length,
            BigDecimal width,
            BigDecimal height,
            BigDecimal weight,
            String category,
            BigDecimal sellingPrice,
            BigDecimal timeInStorage,
            BigDecimal freightCost,
            BigDecimal freightCostUnit,
            BigDecimal otherCosts,
            String otherCostsType,
            String fbaFeeCategory,
            BigDecimal referralFeePercentage
    ) {}
}

