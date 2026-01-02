package com.ServiceTest;

import com.project2.SKUProfitability.DTO.SKUCreateDTO;
import com.project2.SKUProfitability.DTO.SKUDTO;
import com.project2.SKUProfitability.Model.SKU;
import com.project2.SKUProfitability.Repository.SKURepository;
import com.project2.SKUProfitability.Service.FBAFeeCalculatorService;
import com.project2.SKUProfitability.Service.SKUService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.List;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SKUServiceTest {
    @Mock
    private SKURepository skuRepository;
    @Mock
    private FBAFeeCalculatorService feeCalculatorService;
    @InjectMocks
    private SKUService service;

    private SKUCreateDTO validDto;
    private SKU savedSku;

    @BeforeEach
    void setUp() {
        validDto = new SKUCreateDTO("SKU999", "Product", "Desc", new BigDecimal("10"), new BigDecimal("5"), new BigDecimal("2"), new BigDecimal("0.5"), "Category", new BigDecimal("100"));
        savedSku = new SKU(1L, "SKU999", "Product", "Desc", new BigDecimal("10"), new BigDecimal("5"), new BigDecimal("2"), new BigDecimal("0.5"), "Category", new BigDecimal("100"));
    }

    @Test
    void testCreateSKU_SKUExists() {
        when(skuRepository.existsBySku(validDto.sku())).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.createSKU(1L, validDto));
    }

    @Test
    void testCreateSKU_Success() {
        when(skuRepository.existsBySku(validDto.sku())).thenReturn(false);
        when(skuRepository.save(any(SKU.class))).thenReturn(savedSku);
        SKUDTO result = service.createSKU(1L, validDto);
        assertEquals("SKU999", result.sku());
    }

    @Test
    void testGetAllSKUs_Empty() {
        when(skuRepository.findAll()).thenReturn(Collections.emptyList());
        List<SKUDTO> result = service.getAllSKUsByUserId(null);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetSKUById_NotFound() {
        when(skuRepository.findById(2L)).thenReturn(Optional.empty());
        Optional<SKUDTO> result = service.getSKUById(2L, null);
        assertTrue(result.isEmpty());
    }
}
