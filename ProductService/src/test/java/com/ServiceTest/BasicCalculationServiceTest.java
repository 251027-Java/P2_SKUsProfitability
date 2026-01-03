package com.ServiceTest;

import com.p2.ProductService.Service.BasicCalculationService;
import com.p2.ProductService.Util.CalculationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
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
    void testCalculateFBAFulfillmentFee_WithInput() {
        BigDecimal result = service.calculateFBAFulfillmentFee(length, width, height, weight);
        assertNotNull(result);
        assertTrue(result.compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    void testCalculateReferralFee_ZeroPrice() {
        assertEquals(BigDecimal.ZERO, service.calculateReferralFee(BigDecimal.ZERO, "Category"));
    }

    @Test
    void testCalculateStorageFee_WithInput() {
        BigDecimal result = service.calculateStorageFee(length, width, height);
        assertNotNull(result);
        assertTrue(result.compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    void testCalculateStorageFeeJanSep_WithInput() {
        BigDecimal result = service.calculateStorageFeeJanSep(length, width, height, months);
        assertNotNull(result);
        assertTrue(result.compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    void testCalculateStorageFeeOctDec_WithInput() {
        BigDecimal result = service.calculateStorageFeeOctDec(length, width, height, months);
        assertNotNull(result);
        assertTrue(result.compareTo(BigDecimal.ZERO) >= 0);
    }

    @Test
    void testCalculateUnitFreightCost_UnitNotOne() {
        BigDecimal freightCost = new BigDecimal("100");
        BigDecimal freightCostUnit = new BigDecimal("2");
        BigDecimal result = service.calculateUnitFreightCost(freightCost, freightCostUnit, length, width, height);
        assertEquals(freightCost.setScale(2, BigDecimal.ROUND_HALF_UP), result);
    }

    @Test
    void testDetermineSizeTier_NullWeight() {
        assertNull(service.determineSizeTier(length, width, height, null));
    }
}
