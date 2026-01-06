package com.ServiceTest;

import com.p2.product_service.dto.SKUCreateDTO;
import com.p2.product_service.dto.SKUDTO;
import com.p2.product_service.exception.ResourceNotFoundException;
import com.p2.product_service.model.SKU;
import com.p2.product_service.repository.SKURepository;
import com.p2.product_service.service.BasicCalculationService;
import com.p2.product_service.service.SKUService;
import com.p2.product_service.util.DataTransformUtil;
import com.p2.product_service.util.ValidationUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.multipart.MultipartFile;

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
        validDto = new SKUCreateDTO("SKU123", "Product", "Desc", new BigDecimal("10"), new BigDecimal("5"), new BigDecimal("2"), new BigDecimal("0.5"), "Category", new BigDecimal("100"));
        // populate savedSku with fields that match com.p2.product_service.model.SKU
        savedSku = new SKU();
        savedSku.setSkuId(1L);
        savedSku.setSku("SKU123");
        savedSku.setProductName("Product");
        savedSku.setDescription("Desc");
        savedSku.setLength(new BigDecimal("10"));
        savedSku.setWidth(new BigDecimal("5"));
        savedSku.setHeight(new BigDecimal("2"));
        savedSku.setWeight(new BigDecimal("0.5"));
        savedSku.setCategory("Category");
        // sellingPrice field on model
        savedSku.setSellingPrice(new BigDecimal("100"));
    }

//    @Test
//    void createSKU_HappyPath() {
//        when(skuRepository.existsBySku(validDto.sku())).thenReturn(false);
//        when(skuRepository.save(any(SKU.class))).thenReturn(savedSku);
//        SKUDTO result = service.createSKU(validDto);
//        assertNotNull(result);
//        assertEquals("SKU123", result.sku());
//    }
//
//    @Test
//    void createSKU_SKUExists_ThrowsException() {
//        when(skuRepository.existsBySku(validDto.sku())).thenReturn(true);
//        Exception exception = assertThrows(IllegalArgumentException.class, () -> service.createSKU(validDto));
//        assertTrue(exception.getMessage().contains("SKU already exists"));
//    }

    @Test
    void getAllSKUs_HappyPath() {
        when(skuRepository.findAll()).thenReturn(Collections.singletonList(savedSku));
        List<SKUDTO> result = service.getAllSKUs();
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void getSKUById_HappyPath() {
        when(skuRepository.findById(1L)).thenReturn(Optional.of(savedSku));
        Optional<SKUDTO> result = service.getSKUById(1L);
        assertTrue(result.isPresent());
        assertEquals("SKU123", result.get().sku());
    }

    @Test
    void searchBySku_HappyPath() {
        when(skuRepository.findBySku(any(String.class))).thenReturn(Optional.of(savedSku));
        Optional<SKUDTO> result = service.searchBySku("SKU123");
        assertTrue(result.isPresent());
        assertEquals("SKU123", result.get().sku());
    }

    @Test
    void deleteSKU_HappyPath() {
        when(skuRepository.findById(1L)).thenReturn(Optional.of(savedSku));
        doNothing().when(skuRepository).delete(any(SKU.class));
        assertDoesNotThrow(() -> service.deleteSKU(1L));
    }
}
