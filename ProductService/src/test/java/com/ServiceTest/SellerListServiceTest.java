package com.ServiceTest;

import com.p2.ProductService.DTO.SellerListCreateDTO;
import com.p2.ProductService.DTO.SellerListDTO;
import com.p2.ProductService.Model.SellerList;
import com.p2.ProductService.Repository.SellerListRepository;
import com.p2.ProductService.Repository.SellerListItemRepository;
import com.p2.ProductService.Repository.SKURepository;
import com.p2.ProductService.Service.SellerListService;
import com.p2.ProductService.Service.SKUService;
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
    void testCreateList_NameExists() {
        when(listRepository.existsByUserIdAndName(1L, validDto.name())).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.createList(1L, validDto));
    }

    @Test
    void testCreateList_NameTooLong() {
        SellerListCreateDTO longNameDto = new SellerListCreateDTO("A".repeat(201), "Description");
        assertThrows(IllegalArgumentException.class, () -> service.createList(1L, longNameDto));
    }

    @Test
    void testGetAllListsByUserId_Empty() {
        when(listRepository.findByUserId(1L)).thenReturn(Collections.emptyList());
        List<SellerListDTO> result = service.getAllListsByUserId(1L);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetListById_NotFound() {
        when(listRepository.findById(2L)).thenReturn(Optional.empty());
        Optional<SellerListDTO> result = service.getListById(2L, 1L);
        assertTrue(result.isEmpty());
    }
}
