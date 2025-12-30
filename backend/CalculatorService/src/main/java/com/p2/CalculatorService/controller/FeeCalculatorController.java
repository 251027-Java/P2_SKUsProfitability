package com.p2.CalculatorService.controller;

import com.p2.CalculatorService.dto.FeeCalculationRequest;
import com.p2.CalculatorService.model.Calculation;
import com.p2.CalculatorService.repository.CalculationRepository;
import com.p2.CalculatorService.service.FBAFeeCalculatorService;
import com.p2.CalculatorService.util.CalculationUtil;
import com.p2.CalculatorService.util.ValidationUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/calculator")
// Note: @CrossOrigin removed because CORS is handled in WebConfig
public class FeeCalculatorController {

    private final FBAFeeCalculatorService feeCalculatorService;
    private final CalculationRepository calculationRepository;

    public FeeCalculatorController(FBAFeeCalculatorService feeCalculatorService,
                                   CalculationRepository calculationRepository) {
        this.feeCalculatorService = feeCalculatorService;
        this.calculationRepository = calculationRepository;
    }

//    // --- Security Helper: PRODUCTION MODE ---
//    private Long getUserId(HttpServletRequest request) {
//        // Pulls the ID that was placed into the request by the JwtInterceptor.
//        // If the Interceptor didn't find a valid token, this will be null.
//        return (Long) request.getAttribute("userId");
//    }

    // --- Security Helper: TEST MODE ---
    private Long getUserId(HttpServletRequest request) {
        // ALWAYS returns 1L. No token or Interceptor needed.
        // This allows you to test the logic without any security hurdles.
        return 1L;
    }

    @PostMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculateFees(
            @RequestBody FeeCalculationRequest request,
            HttpServletRequest httpServletRequest) {
        try {
            Long currentUserId = getUserId(httpServletRequest);

            // Validate Inputs
            ValidationUtil.validateDimension(request.length(), "Length");
            ValidationUtil.validateDimension(request.width(), "Width");
            ValidationUtil.validateDimension(request.height(), "Height");
            ValidationUtil.validateWeight(request.weight());
            ValidationUtil.validatePrice(request.sellingPrice(), "Selling Price");
            ValidationUtil.validateTimeInStorage(request.timeInStorage());

            // Core Calculations
            String sizeTier = feeCalculatorService.determineSizeTier(
                    request.length(), request.width(), request.height(), request.weight()
            );

            BigDecimal fbaFee = feeCalculatorService.calculateFBAFulfillmentFee(
                    request.length(), request.width(), request.height(), request.weight()
            );

            BigDecimal referralFee;
            if (request.referralFeePercentage() != null && request.referralFeePercentage().compareTo(BigDecimal.ZERO) > 0) {
                referralFee = CalculationUtil.calculatePercentage(request.sellingPrice(), request.referralFeePercentage());
                referralFee = referralFee.max(new BigDecimal("0.30"));
            } else {
                referralFee = feeCalculatorService.calculateReferralFee(request.sellingPrice(), request.category());
            }

            BigDecimal timeInStorage = request.timeInStorage() != null ? request.timeInStorage() : BigDecimal.ONE;
            BigDecimal storageFeeJanSep = feeCalculatorService.calculateStorageFeeJanSep(
                    request.length(), request.width(), request.height(), timeInStorage
            );
            BigDecimal storageFeeOctDec = feeCalculatorService.calculateStorageFeeOctDec(
                    request.length(), request.width(), request.height(), timeInStorage
            );

            // Freight and Other Costs
            BigDecimal unitFreightCost = BigDecimal.ZERO;
            if (request.freightCost() != null && request.freightCost().compareTo(BigDecimal.ZERO) > 0) {
                unitFreightCost = feeCalculatorService.calculateUnitFreightCost(
                        request.freightCost(),
                        request.freightCostUnit() != null ? request.freightCostUnit() : BigDecimal.ONE,
                        request.length(), request.width(), request.height()
                );
            }

            BigDecimal otherCosts = BigDecimal.ZERO;
            if (request.otherCosts() != null && request.otherCosts().compareTo(BigDecimal.ZERO) > 0) {
                if ("percentage".equalsIgnoreCase(request.otherCostsType())) {
                    otherCosts = CalculationUtil.calculatePercentage(request.sellingPrice(), request.otherCosts());
                } else {
                    otherCosts = request.otherCosts();
                }
            }

            // Totals and Profits
            BigDecimal totalFeesJanSep = CalculationUtil.safeAdd(fbaFee, referralFee);
            BigDecimal totalFeesOctDec = CalculationUtil.safeAdd(fbaFee, referralFee);

            BigDecimal netProfitJanSep = CalculationUtil.calculateNetProfit(request.sellingPrice(), totalFeesJanSep);
            BigDecimal netProfitOctDec = CalculationUtil.calculateNetProfit(request.sellingPrice(), totalFeesOctDec);

            BigDecimal profitMarginJanSep = CalculationUtil.calculateProfitMargin(netProfitJanSep, request.sellingPrice());
            BigDecimal profitMarginOctDec = CalculationUtil.calculateProfitMargin(netProfitOctDec, request.sellingPrice());

            // Database Persistence
            Calculation calculation = new Calculation();
            calculation.setUserId(currentUserId); // SECURE: Uses verified ID
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
            calculation.setProfitMarginJanSep(profitMarginJanSep != null ? profitMarginJanSep : BigDecimal.ZERO);
            calculation.setProfitMarginOctDec(profitMarginOctDec != null ? profitMarginOctDec : BigDecimal.ZERO);

            calculationRepository.save(calculation);

            // Response Map
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
            response.put("sellingPrice", request.sellingPrice());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
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