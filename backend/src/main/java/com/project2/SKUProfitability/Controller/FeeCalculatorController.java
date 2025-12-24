package com.project2.SKUProfitability.Controller;

import com.project2.SKUProfitability.Service.FBAFeeCalculatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/calculator")
public class FeeCalculatorController {
    private final FBAFeeCalculatorService feeCalculatorService;
    
    public FeeCalculatorController(FBAFeeCalculatorService feeCalculatorService) {
        this.feeCalculatorService = feeCalculatorService;
    }
    
    @PostMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculateFees(@RequestBody FeeCalculationRequest request) {
        try {
            BigDecimal fbaFee = feeCalculatorService.calculateFBAFulfillmentFee(
                    request.length(),
                    request.width(),
                    request.height(),
                    request.weight()
            );
            
            BigDecimal referralFee = feeCalculatorService.calculateReferralFee(
                    request.sellingPrice(),
                    request.category()
            );
            
            BigDecimal storageFee = feeCalculatorService.calculateStorageFee(
                    request.length(),
                    request.width(),
                    request.height()
            );
            
            BigDecimal totalFees = feeCalculatorService.calculateTotalFees(
                    fbaFee, referralFee, storageFee
            );
            
            BigDecimal netProfit = feeCalculatorService.calculateNetProfit(
                    request.sellingPrice(),
                    request.cost(),
                    totalFees
            );
            
            BigDecimal profitMargin = feeCalculatorService.calculateProfitMargin(
                    request.sellingPrice(),
                    netProfit
            );
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("fbaFulfillmentFee", fbaFee);
            response.put("referralFee", referralFee);
            response.put("storageFee", storageFee);
            response.put("totalFees", totalFees);
            response.put("netProfit", netProfit);
            response.put("profitMargin", profitMargin);
            response.put("sellingPrice", request.sellingPrice());
            response.put("cost", request.cost());
            
            if (request.cost() != null && request.cost().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal roi = feeCalculatorService.calculateROI(request.cost(), netProfit);
                response.put("roi", roi);
            }
            
            if (request.targetROI() != null && request.targetROI().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal maxCost = feeCalculatorService.calculateMaxCostForROI(
                        request.sellingPrice(), totalFees, request.targetROI()
                );
                response.put("maxCost", maxCost);
                response.put("targetROI", request.targetROI());
            }
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    public record FeeCalculationRequest(
            BigDecimal length,
            BigDecimal width,
            BigDecimal height,
            BigDecimal weight,
            String category,
            BigDecimal sellingPrice,
            BigDecimal cost,
            BigDecimal targetROI
    ) {}
}

