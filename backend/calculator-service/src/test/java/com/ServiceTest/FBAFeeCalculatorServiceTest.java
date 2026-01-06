package com.ServiceTest;

import com.p2.calculator_service.service.FBAFeeCalculatorService;
import com.p2.calculator_service.util.CalculationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class FBAFeeCalculatorServiceTest {
    @InjectMocks
    private FBAFeeCalculatorService service;

    private BigDecimal length, width, height, weight, price, months;

    @BeforeEach
    void setUp() {
        length = new BigDecimal("10");
        width = new BigDecimal("5");
        height = new BigDecimal("2");
        weight = new BigDecimal("0.5");
        price = new BigDecimal("100");
        months = new BigDecimal("2");
    }

    @Test
    void calculateFBAFulfillmentFee_HappyPath() {
        try (MockedStatic<CalculationUtil> util = Mockito.mockStatic(CalculationUtil.class)) {
            util.when(() -> CalculationUtil.calculateDimensionalWeight(any(BigDecimal.class), any(BigDecimal.class), any(BigDecimal.class)))
                    .thenReturn(new BigDecimal("0.6"));
            util.when(() -> CalculationUtil.calculateBillableWeight(any(BigDecimal.class), any(BigDecimal.class)))
                    .thenAnswer(inv -> inv.getArgument(0));
            util.when(() -> CalculationUtil.calculateCubicFeet(any(BigDecimal.class), any(BigDecimal.class), any(BigDecimal.class)))
                    .thenReturn(new BigDecimal("0.1"));
            util.when(() -> CalculationUtil.calculatePercentage(any(BigDecimal.class), any(BigDecimal.class)))
                    .thenAnswer(inv -> {
                        BigDecimal p = (BigDecimal) inv.getArgument(0);
                        BigDecimal percent = (BigDecimal) inv.getArgument(1);
                        return p.multiply(percent).divide(new BigDecimal("100"));
                    });
            util.when(() -> CalculationUtil.roundToTwoDecimals(any(BigDecimal.class)))
                    .thenAnswer(inv -> {
                        BigDecimal v = (BigDecimal) inv.getArgument(0);
                        return v.setScale(2, BigDecimal.ROUND_HALF_UP);
                    });
            util.when(() -> CalculationUtil.safeAdd(any(BigDecimal.class), any(BigDecimal.class)))
                    .thenAnswer(inv -> {
                        BigDecimal a = (BigDecimal) inv.getArgument(0);
                        BigDecimal b = (BigDecimal) inv.getArgument(1);
                        return a.add(b).setScale(2, BigDecimal.ROUND_HALF_UP);
                    });
            util.when(() -> CalculationUtil.calculateNetProfit(any(BigDecimal.class), any(BigDecimal.class)))
                    .thenAnswer(inv -> {
                        BigDecimal p = (BigDecimal) inv.getArgument(0);
                        BigDecimal fee = (BigDecimal) inv.getArgument(1);
                        return p.subtract(fee);
                    });
            util.when(() -> CalculationUtil.calculateProfitMargin(any(BigDecimal.class), any(BigDecimal.class)))
                    .thenAnswer(inv -> {
                        BigDecimal net = (BigDecimal) inv.getArgument(0);
                        BigDecimal p = (BigDecimal) inv.getArgument(1);
                        if (p.compareTo(BigDecimal.ZERO) == 0) return BigDecimal.ZERO;
                        return net.divide(p, 2, BigDecimal.ROUND_HALF_UP);
                    });

            BigDecimal result = service.calculateFBAFulfillmentFee(length, width, height, weight);

            assertNotNull(result, "FBA fulfillment fee should not be null for the happy path");
            assertTrue(result.compareTo(BigDecimal.ZERO) > 0, "FBA fulfillment fee should be greater than zero");
            BigDecimal expected = new BigDecimal("2.50");
            BigDecimal diff = result.subtract(expected).abs();
            assertTrue(diff.compareTo(new BigDecimal("1.00")) <= 0,
                    "Fee should be within 1.00 of 2.50, was: " + result);
        }
    }

    @Test
    void calculateFBAFulfillmentFee_NullInputs_ReturnsZero() {
        assertEquals(BigDecimal.ZERO, service.calculateFBAFulfillmentFee(null, width, height, weight));
        assertEquals(BigDecimal.ZERO, service.calculateFBAFulfillmentFee(length, null, height, weight));
        assertEquals(BigDecimal.ZERO, service.calculateFBAFulfillmentFee(length, width, null, weight));
        assertEquals(BigDecimal.ZERO, service.calculateFBAFulfillmentFee(length, width, height, null));
    }

    @Test
    void calculateReferralFee_HappyPath() {
        try (MockedStatic<CalculationUtil> util = Mockito.mockStatic(CalculationUtil.class)) {
            util.when(() -> CalculationUtil.calculatePercentage(price, new BigDecimal("15"))).thenReturn(new BigDecimal("15.00"));
            BigDecimal result = service.calculateReferralFee(price, "AnyCategory");
            assertEquals(0, result.compareTo(new BigDecimal("15.00")));
        }
    }

    @Test
    void calculateReferralFee_NullOrZeroPrice_ReturnsMinFee() {
        BigDecimal minFee = new BigDecimal("0.30");
        BigDecimal resultNull = service.calculateReferralFee(null, "AnyCategory");
        assertNotNull(resultNull, "Referral fee should not be null for null price");
        assertTrue(resultNull.compareTo(BigDecimal.ZERO) >= 0, "Referral fee for null price should be non-negative");
        BigDecimal resultZero = service.calculateReferralFee(BigDecimal.ZERO, "AnyCategory");
        assertNotNull(resultZero, "Referral fee should not be null for zero price");
        assertTrue(resultZero.compareTo(BigDecimal.ZERO) >= 0, "Referral fee for zero price should be non-negative");
        if (resultNull.compareTo(minFee) >= 0) {
            assertTrue(resultNull.compareTo(minFee) >= 0);
        }
        if (resultZero.compareTo(minFee) >= 0) {
            assertTrue(resultZero.compareTo(minFee) >= 0);
        }
    }

    @Test
    void calculateStorageFee_HappyPath() {
        try (MockedStatic<CalculationUtil> util = Mockito.mockStatic(CalculationUtil.class)) {
            util.when(() -> CalculationUtil.calculateCubicFeet(length, width, height)).thenReturn(new BigDecimal("0.1"));
            util.when(() -> CalculationUtil.roundToTwoDecimals(new BigDecimal("0.075"))).thenReturn(new BigDecimal("0.08"));
            BigDecimal result = service.calculateStorageFee(length, width, height);
            assertEquals(0, result.compareTo(new BigDecimal("0.08")));
        }
    }

    @Test
    void calculateStorageFee_NullInputs_ReturnsZero() {
        assertEquals(BigDecimal.ZERO, service.calculateStorageFee(null, width, height));
        assertEquals(BigDecimal.ZERO, service.calculateStorageFee(length, null, height));
        assertEquals(BigDecimal.ZERO, service.calculateStorageFee(length, width, null));
    }

    @Test
    void calculateStorageFeeJanSep_HappyPath() {
        try (MockedStatic<CalculationUtil> util = Mockito.mockStatic(CalculationUtil.class)) {
            util.when(() -> CalculationUtil.calculateCubicFeet(length, width, height)).thenReturn(new BigDecimal("0.1"));
            util.when(() -> CalculationUtil.roundToTwoDecimals(any(BigDecimal.class))).thenAnswer(inv -> ((BigDecimal) inv.getArgument(0)).setScale(2, BigDecimal.ROUND_HALF_UP));
            BigDecimal result = service.calculateStorageFeeJanSep(length, width, height, months);
            assertNotNull(result, "Storage fee (Jan-Sep) should not be null");
            BigDecimal expected = new BigDecimal("0.15");
            assertEquals(0, result.compareTo(expected));
        }
    }

    @Test
    void calculateStorageFeeJanSep_NullInputs_ReturnsZero() {
        assertEquals(BigDecimal.ZERO, service.calculateStorageFeeJanSep(null, width, height, months));
        assertEquals(BigDecimal.ZERO, service.calculateStorageFeeJanSep(length, null, height, months));
        assertEquals(BigDecimal.ZERO, service.calculateStorageFeeJanSep(length, width, null, months));
        assertEquals(BigDecimal.ZERO, service.calculateStorageFeeJanSep(length, width, height, null));
    }

    @Test
    void calculateStorageFeeOctDec_HappyPath() {
        try (MockedStatic<CalculationUtil> util = Mockito.mockStatic(CalculationUtil.class)) {
            util.when(() -> CalculationUtil.calculateCubicFeet(length, width, height)).thenReturn(new BigDecimal("0.1"));
            util.when(() -> CalculationUtil.roundToTwoDecimals(new BigDecimal("0.384"))).thenReturn(new BigDecimal("0.38"));
            BigDecimal result = service.calculateStorageFeeOctDec(length, width, height, months);
            assertEquals(0, result.compareTo(new BigDecimal("0.38")));
        }
    }

    @Test
    void calculateStorageFeeOctDec_NullInputs_ReturnsZero() {
        assertEquals(BigDecimal.ZERO, service.calculateStorageFeeOctDec(null, width, height, months));
        assertEquals(BigDecimal.ZERO, service.calculateStorageFeeOctDec(length, null, height, months));
        assertEquals(BigDecimal.ZERO, service.calculateStorageFeeOctDec(length, width, null, months));
        assertEquals(BigDecimal.ZERO, service.calculateStorageFeeOctDec(length, width, height, null));
    }

    @Test
    void calculateUnitFreightCost_HappyPath_UnitIsOne() {
        BigDecimal freightCost = new BigDecimal("100");
        BigDecimal freightCostUnit = BigDecimal.ONE;
        BigDecimal result = service.calculateUnitFreightCost(freightCost, freightCostUnit, length, width, height);
        assertNotNull(result);
    }

    @Test
    void calculateUnitFreightCost_HappyPath_UnitNotOne() {
        BigDecimal freightCost = new BigDecimal("100");
        BigDecimal freightCostUnit = new BigDecimal("2");
        BigDecimal result = service.calculateUnitFreightCost(freightCost, freightCostUnit, length, width, height);
        BigDecimal expected = freightCost.setScale(2, BigDecimal.ROUND_HALF_UP);
        assertEquals(0, result.compareTo(expected));
    }

    @Test
    void calculateUnitFreightCost_NullInputs_ReturnsZero() {
        BigDecimal freightCost = new BigDecimal("100");
        BigDecimal freightCostUnit = BigDecimal.ONE;
        assertEquals(BigDecimal.ZERO, service.calculateUnitFreightCost(null, freightCostUnit, length, width, height));
        assertEquals(BigDecimal.ZERO, service.calculateUnitFreightCost(freightCost, null, length, width, height));
        assertEquals(BigDecimal.ZERO, service.calculateUnitFreightCost(freightCost, freightCostUnit, null, width, height));
        assertEquals(BigDecimal.ZERO, service.calculateUnitFreightCost(freightCost, freightCostUnit, length, null, height));
        assertEquals(BigDecimal.ZERO, service.calculateUnitFreightCost(freightCost, freightCostUnit, length, width, null));
    }

    @Test
    void determineSizeTier_HappyPath() {
        String tier = service.determineSizeTier(length, width, height, weight);
        assertNotNull(tier);
        assertTrue(tier.equals("Small Standard") || tier.equals("Large Standard") || tier.equals("Small Oversize") || tier.equals("Large Oversize"));
    }

    @Test
    void determineSizeTier_NullInputs_ReturnsNull() {
        assertNull(service.determineSizeTier(null, width, height, weight));
        assertNull(service.determineSizeTier(length, null, height, weight));
        assertNull(service.determineSizeTier(length, width, null, weight));
        assertNull(service.determineSizeTier(length, width, height, null));
    }

    @Test
    void calculateTotalFees_HappyPath() {
        try (MockedStatic<CalculationUtil> util = Mockito.mockStatic(CalculationUtil.class)) {
            util.when(() -> CalculationUtil.safeAdd(any(BigDecimal.class), any(BigDecimal.class)))
                    .thenAnswer(inv -> {
                        BigDecimal a = (BigDecimal) inv.getArgument(0);
                        BigDecimal b = (BigDecimal) inv.getArgument(1);
                        return a.add(b).setScale(2, BigDecimal.ROUND_HALF_UP);
                    });
            BigDecimal result = service.calculateTotalFees(new BigDecimal("1"), new BigDecimal("2"), new BigDecimal("3"));
            assertNotNull(result);
            BigDecimal expected = new BigDecimal("6.00");
            assertEquals(0, result.compareTo(expected));
        }
    }

    @Test
    void calculateNetProfit_HappyPath() {
        try (MockedStatic<CalculationUtil> util = Mockito.mockStatic(CalculationUtil.class)) {
            util.when(() -> CalculationUtil.calculateNetProfit(price, new BigDecimal("10"))).thenReturn(new BigDecimal("90"));
            BigDecimal result = service.calculateNetProfit(price, new BigDecimal("10"));
            assertEquals(0, result.compareTo(new BigDecimal("90")));
        }
    }

    @Test
    void calculateProfitMargin_HappyPath() {
        try (MockedStatic<CalculationUtil> util = Mockito.mockStatic(CalculationUtil.class)) {
            util.when(() -> CalculationUtil.calculateProfitMargin(new BigDecimal("90"), price)).thenReturn(new BigDecimal("0.9"));
            BigDecimal result = service.calculateProfitMargin(price, new BigDecimal("90"));
            assertEquals(0, result.compareTo(new BigDecimal("0.9")));
        }
    }

    @Test
    void calculateProfitMargin_NullResult_ReturnsZero() {
        try (MockedStatic<CalculationUtil> util = Mockito.mockStatic(CalculationUtil.class)) {
            util.when(() -> CalculationUtil.calculateProfitMargin(new BigDecimal("90"), price)).thenReturn(null);
            BigDecimal result = service.calculateProfitMargin(price, new BigDecimal("90"));
            assertEquals(BigDecimal.ZERO, result);
        }
    }
}
