package com.p2.calculator_service.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.p2.calculator_service.dto.FeeCalculationRequest;
import com.p2.calculator_service.model.Calculation;
import com.p2.calculator_service.model.Product;
import com.p2.calculator_service.repository.CalculationRepository;
import com.p2.calculator_service.repository.ProductRepository;
import com.p2.calculator_service.service.FBAFeeCalculatorService;
import com.p2.calculator_service.util.CalculationUtil;
import com.p2.calculator_service.util.ValidationUtil;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/calculator")
public class FeeCalculatorController {

    private final FBAFeeCalculatorService feeCalculatorService;
    private final CalculationRepository calculationRepository;
    private final ProductRepository productRepository;

    public FeeCalculatorController(FBAFeeCalculatorService feeCalculatorService,
                                   CalculationRepository calculationRepository, ProductRepository productRepository) {
        this.feeCalculatorService = feeCalculatorService;
        this.calculationRepository = calculationRepository;
        this.productRepository = productRepository;
    }

    private Long getUserId(HttpServletRequest request) {
        Object attrId = request.getAttribute("userId");
        if (attrId != null) {
            return (Long) attrId;
        }

        String headerId = request.getHeader("userId");
        if (headerId != null && !headerId.isEmpty()) {
            try {
                return Long.parseLong(headerId);
            } catch (NumberFormatException e) {
                return null;
            }
        }

        return null;
    }

    @PostMapping("/calculate")
    public ResponseEntity<Map<String, Object>> calculateFees(
            @RequestBody FeeCalculationRequest request,
            HttpServletRequest httpServletRequest) {
        try {
            Long currentUserId = getUserId(httpServletRequest);

            ValidationUtil.validateDimension(request.length(), "Length");
            ValidationUtil.validateDimension(request.width(), "Width");
            ValidationUtil.validateDimension(request.height(), "Height");
            ValidationUtil.validateWeight(request.weight());
            ValidationUtil.validatePrice(request.sellingPrice(), "Selling Price");
            ValidationUtil.validateTimeInStorage(request.timeInStorage());

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

            BigDecimal totalFeesJanSep = CalculationUtil.safeAdd(fbaFee, referralFee);
            BigDecimal totalFeesOctDec = CalculationUtil.safeAdd(fbaFee, referralFee);

            BigDecimal netProfitJanSep = CalculationUtil.calculateNetProfit(request.sellingPrice(), totalFeesJanSep);
            BigDecimal netProfitOctDec = CalculationUtil.calculateNetProfit(request.sellingPrice(), totalFeesOctDec);

            BigDecimal profitMarginJanSep = CalculationUtil.calculateProfitMargin(netProfitJanSep, request.sellingPrice());
            BigDecimal profitMarginOctDec = CalculationUtil.calculateProfitMargin(netProfitOctDec, request.sellingPrice());

            Calculation calculation = new Calculation();
            calculation.setUserId(currentUserId);
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

            Map<String, Object> response = new HashMap<>();
            response.put("sizeTier", sizeTier);
            response.put("fbaFulfillmentFee", fbaFee);
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

    @PostMapping("/calculate/sku")
    public ResponseEntity<Map<String, Object>> calculateBySku(
            @RequestParam String sku,
            @RequestBody FeeCalculationRequest request,
            HttpServletRequest httpServletRequest) {
        try {
            Product product = productRepository.findBySku(sku)
                    .orElseThrow(() -> new RuntimeException("SKU not found: " + sku));

            BigDecimal length = request.length() != null ? request.length() : product.getLength();
            BigDecimal width = request.width() != null ? request.width() : product.getWidth();
            BigDecimal height = request.height() != null ? request.height() : product.getHeight();
            BigDecimal weight = request.weight() != null ? request.weight() : product.getWeight();
            BigDecimal price = request.sellingPrice() != null ? request.sellingPrice() : product.getSellingPrice();
            String category = request.category() != null ? request.category() : product.getCategory();

            Long currentUserId = getUserId(httpServletRequest);

            ValidationUtil.validateDimension(length, "Length");
            ValidationUtil.validateDimension(width, "Width");
            ValidationUtil.validateDimension(height, "Height");
            ValidationUtil.validateWeight(weight);
            ValidationUtil.validatePrice(price, "Selling Price");

            String sizeTier = feeCalculatorService.determineSizeTier(length, width, height, weight);
            BigDecimal fbaFee = feeCalculatorService.calculateFBAFulfillmentFee(length, width, height, weight);

            BigDecimal referralFee;
            if (request.referralFeePercentage() != null && request.referralFeePercentage().compareTo(BigDecimal.ZERO) > 0) {
                referralFee = CalculationUtil.calculatePercentage(price, request.referralFeePercentage());
                referralFee = referralFee.max(new BigDecimal("0.30"));
            } else {
                referralFee = feeCalculatorService.calculateReferralFee(price, category);
            }

            BigDecimal timeInStorage = request.timeInStorage() != null ? request.timeInStorage() : BigDecimal.ONE;
            BigDecimal storageFeeJanSep = feeCalculatorService.calculateStorageFeeJanSep(length, width, height, timeInStorage);
            BigDecimal storageFeeOctDec = feeCalculatorService.calculateStorageFeeOctDec(length, width, height, timeInStorage);

            BigDecimal unitFreightCost = BigDecimal.ZERO;
            if (request.freightCost() != null && request.freightCost().compareTo(BigDecimal.ZERO) > 0) {
                unitFreightCost = feeCalculatorService.calculateUnitFreightCost(
                        request.freightCost(),
                        request.freightCostUnit() != null ? request.freightCostUnit() : BigDecimal.ONE,
                        length, width, height
                );
            }

            BigDecimal otherCostsValue = BigDecimal.ZERO;
            if (request.otherCosts() != null) {
                if ("percentage".equalsIgnoreCase(request.otherCostsType())) {
                    otherCostsValue = CalculationUtil.calculatePercentage(price, request.otherCosts());
                } else {
                    otherCostsValue = request.otherCosts();
                }
            }

            BigDecimal totalFeesJanSep = fbaFee.add(referralFee).add(storageFeeJanSep).add(unitFreightCost).add(otherCostsValue);
            BigDecimal totalFeesOctDec = fbaFee.add(referralFee).add(storageFeeOctDec).add(unitFreightCost).add(otherCostsValue);

            BigDecimal netProfitJanSep = price.subtract(totalFeesJanSep);
            BigDecimal netProfitOctDec = price.subtract(totalFeesOctDec);

            BigDecimal profitMarginJanSep = CalculationUtil.calculateProfitMargin(netProfitJanSep, price);
            BigDecimal profitMarginOctDec = CalculationUtil.calculateProfitMargin(netProfitOctDec, price);

            BigDecimal roiJanSep = (totalFeesJanSep.compareTo(BigDecimal.ZERO) > 0)
                    ? netProfitJanSep.divide(totalFeesJanSep, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                    : BigDecimal.ZERO;

            BigDecimal roiOctDec = (totalFeesOctDec.compareTo(BigDecimal.ZERO) > 0)
                    ? netProfitOctDec.divide(totalFeesOctDec, 4, java.math.RoundingMode.HALF_UP).multiply(new BigDecimal("100"))
                    : BigDecimal.ZERO;

            Calculation calculation = new Calculation();
            calculation.setUserId(currentUserId);
            calculation.setSku(sku);
            calculation.setLength(length);
            calculation.setWidth(width);
            calculation.setHeight(height);
            calculation.setWeight(weight);
            calculation.setSellingPrice(price);
            calculation.setCategory(category);
            calculation.setTimeInStorage(timeInStorage);

            calculation.setFreightCost(request.freightCost());
            calculation.setFreightCostUnit(request.freightCostUnit());
            calculation.setOtherCosts(request.otherCosts());
            calculation.setOtherCostsType(request.otherCostsType());
            calculation.setReferralFeePercentage(request.referralFeePercentage());

            calculation.setSizeTier(sizeTier);
            calculation.setFbaFeeCategory(sizeTier);
            calculation.setFbaFulfillmentFee(fbaFee);
            calculation.setReferralFee(referralFee);
            calculation.setStorageFeeJanSep(storageFeeJanSep);
            calculation.setStorageFeeOctDec(storageFeeOctDec);
            calculation.setUnitFreightCost(unitFreightCost);
            calculation.setTotalFeesJanSep(totalFeesJanSep);
            calculation.setTotalFeesOctDec(totalFeesOctDec);
            calculation.setNetProfitJanSep(netProfitJanSep);
            calculation.setNetProfitOctDec(netProfitOctDec);

            calculation.setRoiJanSep(roiJanSep);
            calculation.setRoiOctDec(roiOctDec);
            calculation.setProfitMarginJanSep(profitMarginJanSep != null ? profitMarginJanSep : BigDecimal.ZERO);
            calculation.setProfitMarginOctDec(profitMarginOctDec != null ? profitMarginOctDec : BigDecimal.ZERO);

            calculationRepository.save(calculation);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("sku", sku);
            response.put("userId", currentUserId);
            response.put("sizeTier", sizeTier);
            response.put("sellingPrice", price);
            response.put("totalFeesJanSep", totalFeesJanSep);
            response.put("netProfitJanSep", netProfitJanSep);
            response.put("profitMarginJanSep", profitMarginJanSep + "%");
            response.put("roiJanSep", roiJanSep + "%");

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