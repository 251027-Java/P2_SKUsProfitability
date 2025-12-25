package com.project2.SKUProfitability.Controller;

import com.project2.SKUProfitability.Service.FBAFeeCalculatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
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
            String sizeTier = feeCalculatorService.determineSizeTier(
                    request.length(),
                    request.width(),
                    request.height(),
                    request.weight()
            );
            
            BigDecimal fbaFee = feeCalculatorService.calculateFBAFulfillmentFee(
                    request.length(),
                    request.width(),
                    request.height(),
                    request.weight()
            );
            
            BigDecimal referralFee;
            if (request.referralFeePercentage() != null && request.referralFeePercentage().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal referralPercentage = request.referralFeePercentage().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
                referralFee = request.sellingPrice().multiply(referralPercentage);
                referralFee = referralFee.max(new BigDecimal("0.30"));
            } else {
                referralFee = feeCalculatorService.calculateReferralFee(
                        request.sellingPrice(),
                        request.category()
                );
            }
            
            BigDecimal timeInStorage = request.timeInStorage() != null ? request.timeInStorage() : BigDecimal.ONE;
            BigDecimal storageFeeJanSep = feeCalculatorService.calculateStorageFeeJanSep(
                    request.length(),
                    request.width(),
                    request.height(),
                    timeInStorage
            );
            BigDecimal storageFeeOctDec = feeCalculatorService.calculateStorageFeeOctDec(
                    request.length(),
                    request.width(),
                    request.height(),
                    timeInStorage
            );
            
            BigDecimal unitFreightCost = BigDecimal.ZERO;
            if (request.freightCost() != null && request.freightCost().compareTo(BigDecimal.ZERO) > 0) {
                unitFreightCost = feeCalculatorService.calculateUnitFreightCost(
                        request.freightCost(),
                        request.freightCostUnit() != null ? request.freightCostUnit() : BigDecimal.ONE,
                        request.length(),
                        request.width(),
                        request.height()
                );
            }
            
            BigDecimal otherCosts = BigDecimal.ZERO;
            if (request.otherCosts() != null && request.otherCosts().compareTo(BigDecimal.ZERO) > 0) {
                if (request.otherCostsType() != null && request.otherCostsType().equals("percentage")) {
                    BigDecimal otherCostsPercentage = request.otherCosts().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);
                    otherCosts = request.sellingPrice().multiply(otherCostsPercentage);
                } else {
                    otherCosts = request.otherCosts();
                }
            }
            
            BigDecimal totalFeesJanSep = fbaFee.add(referralFee).add(storageFeeJanSep).add(unitFreightCost).add(otherCosts);
            BigDecimal totalFeesOctDec = fbaFee.add(referralFee).add(storageFeeOctDec).add(unitFreightCost).add(otherCosts);
            
            BigDecimal cost = request.cost() != null ? request.cost() : BigDecimal.ZERO;
            BigDecimal netProfitJanSep = request.sellingPrice().subtract(cost).subtract(totalFeesJanSep);
            BigDecimal netProfitOctDec = request.sellingPrice().subtract(cost).subtract(totalFeesOctDec);
            
            BigDecimal profitMarginJanSep = feeCalculatorService.calculateProfitMargin(
                    request.sellingPrice(),
                    netProfitJanSep
            );
            BigDecimal profitMarginOctDec = feeCalculatorService.calculateProfitMargin(
                    request.sellingPrice(),
                    netProfitOctDec
            );
            
            BigDecimal roiJanSep = BigDecimal.ZERO;
            BigDecimal roiOctDec = BigDecimal.ZERO;
            if (cost.compareTo(BigDecimal.ZERO) > 0) {
                roiJanSep = feeCalculatorService.calculateROI(cost, netProfitJanSep);
                roiOctDec = feeCalculatorService.calculateROI(cost, netProfitOctDec);
            }
            
            Map<String, Object> response = new java.util.HashMap<>();
            response.put("sizeTier", sizeTier);
            response.put("fbaFulfillmentFee", fbaFee);
            response.put("referralFee", referralFee);
            response.put("storageFeeJanSep", storageFeeJanSep);
            response.put("storageFeeOctDec", storageFeeOctDec);
            response.put("unitFreightCost", unitFreightCost);
            response.put("otherCosts", otherCosts);
            response.put("totalFeesJanSep", totalFeesJanSep);
            response.put("totalFeesOctDec", totalFeesOctDec);
            response.put("netProfitJanSep", netProfitJanSep);
            response.put("netProfitOctDec", netProfitOctDec);
            response.put("profitMarginJanSep", profitMarginJanSep);
            response.put("profitMarginOctDec", profitMarginOctDec);
            response.put("roiJanSep", roiJanSep);
            response.put("roiOctDec", roiOctDec);
            response.put("sellingPrice", request.sellingPrice());
            response.put("cost", request.cost());
            
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
            BigDecimal outboundShippingWeight,
            String category,
            BigDecimal sellingPrice,
            BigDecimal cost,
            BigDecimal targetROI,
            BigDecimal timeInStorage,
            BigDecimal freightCost,
            BigDecimal freightCostUnit,
            BigDecimal otherCosts,
            String otherCostsType,
            String fbaFeeCategory,
            BigDecimal referralFeePercentage
    ) {}
}

