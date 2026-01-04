package com.ServiceTest;

import com.p2.product_service.dto.SellerListCreateDTO;
import com.p2.product_service.dto.SellerListDTO;
import com.p2.product_service.model.SellerList;
import com.p2.product_service.repository.SellerListRepository;
import com.p2.product_service.repository.SellerListItemRepository;
import com.p2.product_service.repository.SKURepository;
import com.p2.product_service.service.SellerListService;
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

import java.util.Optional;
import java.util.List;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SellerListServiceTest {
    @Mock
    private SellerListRepository listRepository;
    @Mock
    private SellerListItemRepository listItemRepository;
    @Mock
    private SKURepository skuRepository;
    @Mock
    private SKUService skuService;
    @InjectMocks
    private SellerListService service;

    private SellerListCreateDTO validDto;
    private SellerList savedList;

    @BeforeEach
    void setUp() {
        validDto = new SellerListCreateDTO("ListName", "Description");
        savedList = new SellerList(1L, "ListName", "Description");
        savedList.setUserId(1L);
    }

    @Test
    void createList_HappyPath() {
        when(listRepository.existsByUserIdAndName(1L, validDto.name())).thenReturn(false);
        when(listRepository.save(any(SellerList.class))).thenReturn(savedList);
        SellerListDTO result = service.createList(1L, validDto);
        assertNotNull(result);
        assertEquals("ListName", result.name());
    }

    @Test
    void createList_NameTooLong_ThrowsException() {
        SellerListCreateDTO longNameDto = new SellerListCreateDTO("A".repeat(201), "Description");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> service.createList(1L, longNameDto));
        assertTrue(exception.getMessage().contains("200 characters or less"));
    }

    @Test
    void createList_NameExists_ThrowsException() {
        when(listRepository.existsByUserIdAndName(1L, validDto.name())).thenReturn(true);
        Exception exception = assertThrows(IllegalArgumentException.class, () -> service.createList(1L, validDto));
        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    void getAllListsByUserId_HappyPath() {
        when(listRepository.findByUserId(1L)).thenReturn(Collections.singletonList(savedList));
        List<SellerListDTO> result = service.getAllListsByUserId(1L);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void getListById_HappyPath() {
        when(listRepository.findById(1L)).thenReturn(Optional.of(savedList));
        Optional<SellerListDTO> result = service.getListById(1L, 1L);
        assertTrue(result.isPresent());
        assertEquals("ListName", result.get().name());
    }

    @Test
    void updateList_HappyPath() {
        when(listRepository.findById(1L)).thenReturn(Optional.of(savedList));
        when(listRepository.save(any(SellerList.class))).thenReturn(savedList);
        SellerListCreateDTO updateDto = new SellerListCreateDTO("NewName", "NewDesc");
        SellerListDTO result = service.updateList(1L, 1L, updateDto);
        assertNotNull(result);
        assertEquals("NewName", result.name());
    }

    @Test
    void deleteList_HappyPath() {
        when(listRepository.findById(1L)).thenReturn(Optional.of(savedList));
        doNothing().when(listRepository).delete(any(SellerList.class));
        assertDoesNotThrow(() -> service.deleteList(1L, 1L));
    }
}
