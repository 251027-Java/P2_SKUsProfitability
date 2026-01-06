package com.ServiceTest;

import com.p2.product_service.service.BasicCalculationService;
import com.p2.product_service.util.CalculationUtil;
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
public class BasicCalculationServiceTest {
    @InjectMocks
    private BasicCalculationService service;

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
    void calculateFBAFulfillmentFee_WithInput() {
        BigDecimal result = service.calculateFBAFulfillmentFee(length, width, height, weight);
        assertNotNull(result);
        assertTrue(result.compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    void calculateReferralFee_HappyPath() {
        try (MockedStatic<CalculationUtil> util = Mockito.mockStatic(CalculationUtil.class)) {
            util.when(() -> CalculationUtil.calculatePercentage(price, new BigDecimal("15"))).thenReturn(new BigDecimal("15.00"));
            BigDecimal result = service.calculateReferralFee(price, "AnyCategory");
            assertEquals(new BigDecimal("15.00"), result);
        }
    }

    @Test
    void calculateReferralFee_NullOrZeroPrice_ReturnsZero() {
        assertEquals(BigDecimal.ZERO, service.calculateReferralFee(null, "AnyCategory"));
        assertEquals(BigDecimal.ZERO, service.calculateReferralFee(BigDecimal.ZERO, "AnyCategory"));
    }

    @Test
    void calculateStorageFee_HappyPath() {
        try (MockedStatic<CalculationUtil> util = Mockito.mockStatic(CalculationUtil.class)) {
            util.when(() -> CalculationUtil.calculateCubicFeet(length, width, height)).thenReturn(new BigDecimal("0.1"));
            util.when(() -> CalculationUtil.roundToTwoDecimals(new BigDecimal("0.075"))).thenReturn(new BigDecimal("0.08"));
            BigDecimal result = service.calculateStorageFee(length, width, height);
            assertEquals(new BigDecimal("0.08"), result);
        }
    }

    @Test
    void calculateStorageFee_NullInputs_ReturnsZero() {
        assertEquals(BigDecimal.ZERO, service.calculateStorageFee(null, width, height));
        assertEquals(BigDecimal.ZERO, service.calculateStorageFee(length, null, height));
        assertEquals(BigDecimal.ZERO, service.calculateStorageFee(length, width, null));
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
            assertEquals(new BigDecimal("0.38"), result);
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
        assertEquals(freightCost.setScale(2, BigDecimal.ROUND_HALF_UP), result);
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
    void calculateNetProfit_HappyPath() {
        try (MockedStatic<CalculationUtil> util = Mockito.mockStatic(CalculationUtil.class)) {
            util.when(() -> CalculationUtil.calculateNetProfit(price, new BigDecimal("10"))).thenReturn(new BigDecimal("90"));
            BigDecimal result = service.calculateNetProfit(price, new BigDecimal("10"));
            assertEquals(new BigDecimal("90"), result);
        }
    }
}
