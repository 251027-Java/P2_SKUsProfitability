package com.p2.calculator_service.service;

import com.p2.calculator_service.util.CalculationUtil;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class FBAFeeCalculatorService {
    private static final BigDecimal SMALL_STANDARD_MAX_LENGTH = new BigDecimal("15");
    private static final BigDecimal SMALL_STANDARD_MAX_WIDTH = new BigDecimal("12");
    private static final BigDecimal SMALL_STANDARD_MAX_HEIGHT = new BigDecimal("0.75");
    private static final BigDecimal SMALL_STANDARD_MAX_WEIGHT = new BigDecimal("0.75");
    
    private static final BigDecimal LARGE_STANDARD_MAX_LENGTH = new BigDecimal("18");
    private static final BigDecimal LARGE_STANDARD_MAX_WIDTH = new BigDecimal("14");
    private static final BigDecimal LARGE_STANDARD_MAX_HEIGHT = new BigDecimal("8");
    private static final BigDecimal LARGE_STANDARD_MAX_WEIGHT = new BigDecimal("20");
    
    private static final BigDecimal SMALL_OVERSIZE_MAX_LENGTH = new BigDecimal("60");
    private static final BigDecimal SMALL_OVERSIZE_MAX_WIDTH = new BigDecimal("30");
    private static final BigDecimal SMALL_OVERSIZE_MAX_HEIGHT = new BigDecimal("30");
    private static final BigDecimal SMALL_OVERSIZE_MAX_WEIGHT = new BigDecimal("70");
    
    public BigDecimal calculateFBAFulfillmentFee(BigDecimal length, BigDecimal width, 
                                                  BigDecimal height, BigDecimal weight) {
        if (length == null || width == null || height == null || weight == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal dimensionalWeight = CalculationUtil.calculateDimensionalWeight(length, width, height);
        BigDecimal billableWeight = CalculationUtil.calculateBillableWeight(weight, dimensionalWeight);
        String sizeTier = determineSizeTier(length, width, height, weight);
        
        return CalculationUtil.roundToTwoDecimals(calculateFeeBySizeTier(sizeTier, billableWeight));
    }
    
    public BigDecimal calculateReferralFee(BigDecimal sellingPrice, String category) {
        if (sellingPrice == null || sellingPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal referralPercentage = new BigDecimal("15");
        BigDecimal calculatedFee = CalculationUtil.calculatePercentage(sellingPrice, referralPercentage);
        return calculatedFee.max(new BigDecimal("0.30"));
    }
    
    public BigDecimal calculateStorageFee(BigDecimal length, BigDecimal width, BigDecimal height) {
        if (length == null || width == null || height == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal cubicFeet = CalculationUtil.calculateCubicFeet(length, width, height);
        BigDecimal storageFeePerCubicFoot = new BigDecimal("0.75");
        
        return CalculationUtil.roundToTwoDecimals(cubicFeet.multiply(storageFeePerCubicFoot));
    }
    
    public BigDecimal calculateStorageFeeJanSep(BigDecimal length, BigDecimal width, BigDecimal height, BigDecimal months) {
        if (length == null || width == null || height == null || months == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal cubicFeet = CalculationUtil.calculateCubicFeet(length, width, height);
        BigDecimal storageFeePerCubicFoot = new BigDecimal("0.75");
        return CalculationUtil.roundToTwoDecimals(cubicFeet.multiply(storageFeePerCubicFoot).multiply(months));
    }
    
    public BigDecimal calculateStorageFeeOctDec(BigDecimal length, BigDecimal width, BigDecimal height, BigDecimal months) {
        if (length == null || width == null || height == null || months == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal cubicFeet = CalculationUtil.calculateCubicFeet(length, width, height);
        BigDecimal storageFeePerCubicFoot = new BigDecimal("1.92");
        return CalculationUtil.roundToTwoDecimals(cubicFeet.multiply(storageFeePerCubicFoot).multiply(months));
    }
    
    public BigDecimal calculateUnitFreightCost(BigDecimal freightCost, BigDecimal freightCostUnit, 
                                                BigDecimal length, BigDecimal width, BigDecimal height) {
        if (freightCost == null || freightCostUnit == null || 
            length == null || width == null || height == null) {
            return BigDecimal.ZERO;
        }
        
        if (freightCostUnit.compareTo(new BigDecimal("1")) == 0) {
            BigDecimal cubicInches = length.multiply(width).multiply(height);
            BigDecimal cubicMeters = cubicInches.divide(new BigDecimal("61023.7"), 4, RoundingMode.HALF_UP);
            return freightCost.multiply(cubicMeters).setScale(2, RoundingMode.HALF_UP);
        }
        
        return freightCost.setScale(2, RoundingMode.HALF_UP);
    }
    
    public String determineSizeTier(BigDecimal length, BigDecimal width, 
                                     BigDecimal height, BigDecimal weight) {
        if (length == null || width == null || height == null || weight == null) {
            return null;
        }
        
        if (length.compareTo(SMALL_STANDARD_MAX_LENGTH) <= 0 &&
            width.compareTo(SMALL_STANDARD_MAX_WIDTH) <= 0 &&
            height.compareTo(SMALL_STANDARD_MAX_HEIGHT) <= 0 &&
            weight.compareTo(SMALL_STANDARD_MAX_WEIGHT) <= 0) {
            return "Small Standard";
        }
        
        if (length.compareTo(LARGE_STANDARD_MAX_LENGTH) <= 0 &&
            width.compareTo(LARGE_STANDARD_MAX_WIDTH) <= 0 &&
            height.compareTo(LARGE_STANDARD_MAX_HEIGHT) <= 0 &&
            weight.compareTo(LARGE_STANDARD_MAX_WEIGHT) <= 0) {
            return "Large Standard";
        }
        
        if (length.compareTo(SMALL_OVERSIZE_MAX_LENGTH) <= 0 &&
            width.compareTo(SMALL_OVERSIZE_MAX_WIDTH) <= 0 &&
            height.compareTo(SMALL_OVERSIZE_MAX_HEIGHT) <= 0 &&
            weight.compareTo(SMALL_OVERSIZE_MAX_WEIGHT) <= 0) {
            return "Small Oversize";
        }
        
        return "Large Oversize";
    }
    
    private BigDecimal calculateFeeBySizeTier(String sizeTier, BigDecimal billableWeight) {
        BigDecimal baseFee;
        
        switch (sizeTier) {
            case "Small Standard":
                baseFee = new BigDecimal("2.50");
                if (billableWeight.compareTo(new BigDecimal("0.75")) > 0) {
                    BigDecimal excessWeight = billableWeight.subtract(new BigDecimal("0.75"));
                    baseFee = baseFee.add(excessWeight.multiply(new BigDecimal("0.38")));
                }
                break;
                
            case "Large Standard":
                baseFee = new BigDecimal("3.31");
                if (billableWeight.compareTo(BigDecimal.ONE) > 0) {
                    BigDecimal excessWeight = billableWeight.subtract(BigDecimal.ONE);
                    baseFee = baseFee.add(excessWeight.multiply(new BigDecimal("0.38")));
                }
                break;
                
            case "Small Oversize":
                baseFee = new BigDecimal("8.13");
                baseFee = baseFee.add(billableWeight.multiply(new BigDecimal("0.38")));
                break;
                
            case "Large Oversize":
                baseFee = new BigDecimal("10.59");
                baseFee = baseFee.add(billableWeight.multiply(new BigDecimal("0.38")));
                break;
                
            default:
                baseFee = new BigDecimal("3.31");
        }
        
        return baseFee.setScale(2, RoundingMode.HALF_UP);
    }
    
    public BigDecimal calculateTotalFees(BigDecimal fbaFee, BigDecimal referralFee, BigDecimal storageFee) {
        BigDecimal total = CalculationUtil.safeAdd(fbaFee, referralFee);
        return CalculationUtil.safeAdd(total, storageFee);
    }
    
    public BigDecimal calculateNetProfit(BigDecimal sellingPrice, BigDecimal totalFees) {
        return CalculationUtil.calculateNetProfit(sellingPrice, totalFees);
    }
    
    public BigDecimal calculateProfitMargin(BigDecimal sellingPrice, BigDecimal netProfit) {
        BigDecimal margin = CalculationUtil.calculateProfitMargin(netProfit, sellingPrice);
        return margin != null ? margin : BigDecimal.ZERO;
    }
    
}
