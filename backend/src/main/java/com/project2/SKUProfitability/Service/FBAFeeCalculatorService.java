package com.project2.SKUProfitability.Service;

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
        
        BigDecimal dimensionalWeight = length.multiply(width).multiply(height)
                .divide(new BigDecimal("166"), 2, RoundingMode.HALF_UP);
        
        BigDecimal billableWeight = weight.max(dimensionalWeight);
        String sizeTier = determineSizeTier(length, width, height, weight);
        
        return calculateFeeBySizeTier(sizeTier, billableWeight);
    }
    
    public BigDecimal calculateReferralFee(BigDecimal sellingPrice, String category) {
        if (sellingPrice == null || sellingPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal referralPercentage = new BigDecimal("0.15");
        BigDecimal calculatedFee = sellingPrice.multiply(referralPercentage);
        return calculatedFee.max(new BigDecimal("0.30"));
    }
    
    public BigDecimal calculateStorageFee(BigDecimal length, BigDecimal width, BigDecimal height) {
        if (length == null || width == null || height == null) {
            return BigDecimal.ZERO;
        }
        
        // Calculate cubic feet
        BigDecimal cubicFeet = length.multiply(width).multiply(height)
                .divide(new BigDecimal("1728"), 4, RoundingMode.HALF_UP);
        
        BigDecimal storageFeePerCubicFoot = new BigDecimal("0.75");
        
        return cubicFeet.multiply(storageFeePerCubicFoot);
    }
    
    public BigDecimal calculateStorageFeeJanSep(BigDecimal length, BigDecimal width, BigDecimal height, BigDecimal months) {
        if (length == null || width == null || height == null || months == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal cubicFeet = length.multiply(width).multiply(height)
                .divide(new BigDecimal("1728"), 4, RoundingMode.HALF_UP);
        
        BigDecimal storageFeePerCubicFoot = new BigDecimal("0.75");
        return cubicFeet.multiply(storageFeePerCubicFoot).multiply(months).setScale(2, RoundingMode.HALF_UP);
    }
    
    public BigDecimal calculateStorageFeeOctDec(BigDecimal length, BigDecimal width, BigDecimal height, BigDecimal months) {
        if (length == null || width == null || height == null || months == null) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal cubicFeet = length.multiply(width).multiply(height)
                .divide(new BigDecimal("1728"), 4, RoundingMode.HALF_UP);
        
        BigDecimal storageFeePerCubicFoot = new BigDecimal("1.92");
        return cubicFeet.multiply(storageFeePerCubicFoot).multiply(months).setScale(2, RoundingMode.HALF_UP);
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
        if (fbaFee == null) fbaFee = BigDecimal.ZERO;
        if (referralFee == null) referralFee = BigDecimal.ZERO;
        if (storageFee == null) storageFee = BigDecimal.ZERO;
        
        return fbaFee.add(referralFee).add(storageFee).setScale(2, RoundingMode.HALF_UP);
    }
    
    public BigDecimal calculateNetProfit(BigDecimal sellingPrice, BigDecimal cost, BigDecimal totalFees) {
        if (sellingPrice == null) sellingPrice = BigDecimal.ZERO;
        if (cost == null) cost = BigDecimal.ZERO;
        if (totalFees == null) totalFees = BigDecimal.ZERO;
        
        return sellingPrice.subtract(cost).subtract(totalFees).setScale(2, RoundingMode.HALF_UP);
    }
    
    public BigDecimal calculateProfitMargin(BigDecimal sellingPrice, BigDecimal netProfit) {
        if (sellingPrice == null || sellingPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (netProfit == null) {
            return BigDecimal.ZERO;
        }
        
        return netProfit.divide(sellingPrice, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    public BigDecimal calculateROI(BigDecimal cost, BigDecimal netProfit) {
        if (cost == null || cost.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (netProfit == null) {
            return BigDecimal.ZERO;
        }
        
        return netProfit.divide(cost, 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"))
                .setScale(2, RoundingMode.HALF_UP);
    }
    
    public BigDecimal calculateMaxCostForROI(BigDecimal sellingPrice, BigDecimal totalFees, BigDecimal targetROIPercentage) {
        if (sellingPrice == null || sellingPrice.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        if (totalFees == null) {
            totalFees = BigDecimal.ZERO;
        }
        if (targetROIPercentage == null || targetROIPercentage.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        
        BigDecimal netProfitAfterFees = sellingPrice.subtract(totalFees);
        BigDecimal roiMultiplier = BigDecimal.ONE.add(targetROIPercentage.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP));
        
        BigDecimal maxCost = netProfitAfterFees.divide(roiMultiplier, 4, RoundingMode.HALF_UP);
        return maxCost.setScale(2, RoundingMode.HALF_UP);
    }
}

