package com.ServiceTest;

import com.p2.ProductService.DTO.SKUCreateDTO;
import com.p2.ProductService.DTO.SKUDTO;
import com.p2.ProductService.Model.SKU;
import com.p2.ProductService.Repository.SKURepository;
import com.p2.ProductService.Service.BasicCalculationService;
import com.p2.ProductService.Service.SKUService;
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
    private BasicCalculationService calculationService;
    @InjectMocks
    private SKUService service;

    private SKUCreateDTO validDto;
    private SKU savedSku;

    @BeforeEach
    void setUp() {
        validDto = new SKUCreateDTO("SKU999", "Product", "Desc", new BigDecimal("10"), new BigDecimal("5"), new BigDecimal("2"), new BigDecimal("0.5"), "Category", new BigDecimal("100"));
        savedSku = new SKU("SKU999", "Product", "Desc", new BigDecimal("10"), new BigDecimal("5"), new BigDecimal("2"), new BigDecimal("0.5"), "Category", new BigDecimal("100"));
        savedSku.setSkuId(1L);
    }

    @Test
    void testCreateSKU_SKUExists() {
        when(skuRepository.existsBySku(validDto.sku())).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.createSKU(validDto));
    }

    @Test
    void testCreateSKU_Success() {
        when(skuRepository.existsBySku(validDto.sku())).thenReturn(false);
        when(skuRepository.save(any(SKU.class))).thenReturn(savedSku);
        SKUDTO result = service.createSKU(validDto);
        assertEquals("SKU999", result.sku());
    }

    @Test
    void testGetAllSKUs_Empty() {
        when(skuRepository.findAll()).thenReturn(Collections.emptyList());
        List<SKUDTO> result = service.getAllSKUs();
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetSKUById_NotFound() {
        when(skuRepository.findById(2L)).thenReturn(Optional.empty());
        Optional<SKUDTO> result = service.getSKUById(2L);
        assertTrue(result.isEmpty());
    }
}
