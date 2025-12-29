package com.p2.CalculatorService.controller;

import com.p2.CalculatorService.dto.FeeCalculationRequest;
import com.p2.CalculatorService.model.Calculation;
import com.p2.CalculatorService.repository.CalculationRepository;
import com.p2.CalculatorService.service.FBAFeeCalculatorService;
import com.p2.CalculatorService.util.CalculationUtil;
import com.p2.CalculatorService.util.ValidationUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/calculator")
@CrossOrigin(origins = {"http://localhost:8080", "http://localhost:3000"})
public class FeeCalculatorController {
    
    private final FBAFeeCalculatorService feeCalculatorService;
    private final CalculationRepository calculationRepository;
    
    public FeeCalculatorController(FBAFeeCalculatorService feeCalculatorService,
                                   CalculationRepository calculationRepository) {
        this.feeCalculatorService = feeCalculatorService;
        this.calculationRepository = calculationRepository;
    }
    
    @PostMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculateFees(@RequestBody FeeCalculationRequest request) {
        try {
            ValidationUtil.validateDimension(request.length(), "Length");
            ValidationUtil.validateDimension(request.width(), "Width");
            ValidationUtil.validateDimension(request.height(), "Height");
            ValidationUtil.validateWeight(request.weight());
            ValidationUtil.validatePrice(request.sellingPrice(), "Selling Price");
            ValidationUtil.validateTimeInStorage(request.timeInStorage());
            
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
                referralFee = CalculationUtil.calculatePercentage(request.sellingPrice(), request.referralFeePercentage());
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
                    otherCosts = CalculationUtil.calculatePercentage(request.sellingPrice(), request.otherCosts());
                } else {
                    otherCosts = request.otherCosts();
                }
            }
            
            BigDecimal totalFeesJanSep = CalculationUtil.safeAdd(fbaFee, referralFee);
            totalFeesJanSep = CalculationUtil.safeAdd(totalFeesJanSep, storageFeeJanSep);
            totalFeesJanSep = CalculationUtil.safeAdd(totalFeesJanSep, unitFreightCost);
            totalFeesJanSep = CalculationUtil.safeAdd(totalFeesJanSep, otherCosts);
            
            BigDecimal totalFeesOctDec = CalculationUtil.safeAdd(fbaFee, referralFee);
            totalFeesOctDec = CalculationUtil.safeAdd(totalFeesOctDec, storageFeeOctDec);
            totalFeesOctDec = CalculationUtil.safeAdd(totalFeesOctDec, unitFreightCost);
            totalFeesOctDec = CalculationUtil.safeAdd(totalFeesOctDec, otherCosts);
            
            BigDecimal netProfitJanSep = CalculationUtil.calculateNetProfit(request.sellingPrice(), totalFeesJanSep);
            BigDecimal netProfitOctDec = CalculationUtil.calculateNetProfit(request.sellingPrice(), totalFeesOctDec);
            
            BigDecimal profitMarginJanSep = CalculationUtil.calculateProfitMargin(netProfitJanSep, request.sellingPrice());
            BigDecimal profitMarginOctDec = CalculationUtil.calculateProfitMargin(netProfitOctDec, request.sellingPrice());
            if (profitMarginJanSep == null) profitMarginJanSep = BigDecimal.ZERO;
            if (profitMarginOctDec == null) profitMarginOctDec = BigDecimal.ZERO;
            
            BigDecimal roiJanSep = BigDecimal.ZERO;
            BigDecimal roiOctDec = BigDecimal.ZERO;
            if (otherCosts != null && otherCosts.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal calculatedRoiJanSep = CalculationUtil.calculateROI(netProfitJanSep, otherCosts);
                BigDecimal calculatedRoiOctDec = CalculationUtil.calculateROI(netProfitOctDec, otherCosts);
                roiJanSep = calculatedRoiJanSep != null ? calculatedRoiJanSep : BigDecimal.ZERO;
                roiOctDec = calculatedRoiOctDec != null ? calculatedRoiOctDec : BigDecimal.ZERO;
            }
            
            // Save calculation to database
            Calculation calculation = new Calculation();
            calculation.setUserId(request.userId());
            calculation.setLength(request.length());
            calculation.setWidth(request.width());
            calculation.setHeight(request.height());
            calculation.setWeight(request.weight());
            calculation.setSellingPrice(request.sellingPrice());
            calculation.setCategory(request.category());
            calculation.setTimeInStorage(timeInStorage);
            calculation.setFreightCost(request.freightCost());
            calculation.setFreightCostUnit(request.freightCostUnit());
            calculation.setOtherCosts(request.otherCosts());
            calculation.setOtherCostsType(request.otherCostsType());
            calculation.setFbaFeeCategory(request.fbaFeeCategory());
            calculation.setReferralFeePercentage(request.referralFeePercentage());
            calculation.setSizeTier(sizeTier);
            calculation.setFbaFulfillmentFee(fbaFee);
            calculation.setReferralFee(referralFee);
            calculation.setStorageFeeJanSep(storageFeeJanSep);
            calculation.setStorageFeeOctDec(storageFeeOctDec);
            calculation.setUnitFreightCost(unitFreightCost);
            calculation.setTotalFeesJanSep(totalFeesJanSep);
            calculation.setTotalFeesOctDec(totalFeesOctDec);
            calculation.setNetProfitJanSep(netProfitJanSep);
            calculation.setNetProfitOctDec(netProfitOctDec);
            calculation.setProfitMarginJanSep(profitMarginJanSep);
            calculation.setProfitMarginOctDec(profitMarginOctDec);
            calculation.setRoiJanSep(roiJanSep);
            calculation.setRoiOctDec(roiOctDec);
            
            calculationRepository.save(calculation);
            
            Map<String, Object> response = new HashMap<>();
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
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
            "status", "UP",
            "service", "CalculatorService"
        ));
    }
}
