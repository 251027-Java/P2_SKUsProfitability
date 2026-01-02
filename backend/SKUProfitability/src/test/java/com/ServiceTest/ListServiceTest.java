package com.ServiceTest;

import com.project2.SKUProfitability.DTO.ListCreateDTO;
import com.project2.SKUProfitability.DTO.ListDTO;
import com.project2.SKUProfitability.Model.List;
import com.project2.SKUProfitability.Repository.ListRepository;
import com.project2.SKUProfitability.Repository.ListItemRepository;
import com.project2.SKUProfitability.Repository.SKURepository;
import com.project2.SKUProfitability.Service.ListService;
import com.project2.SKUProfitability.Service.SKUService;
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
public class ListServiceTest {
    @Mock
    private ListRepository listRepository;
    @Mock
    private ListItemRepository listItemRepository;
    @Mock
    private SKURepository skuRepository;
    @Mock
    private SKUService skuService;
    @InjectMocks
    private ListService service;

    private ListCreateDTO validDto;
    private List savedList;

    @BeforeEach
    void setUp() {
        validDto = new ListCreateDTO("ListName", "Description");
        savedList = new List(1L, "ListName", "Description");
        savedList.setUserId(1L);
    }

    @Test
    void testCreateList_NameExists() {
        when(listRepository.existsByUserIdAndName(1L, validDto.name())).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> service.createList(1L, validDto));
    }

    @Test
    void testCreateList_NameTooLong() {
        ListCreateDTO longNameDto = new ListCreateDTO("A".repeat(201), "Description");
        assertThrows(IllegalArgumentException.class, () -> service.createList(1L, longNameDto));
    }

    @Test
    void testGetAllListsByUserId_Empty() {
        when(listRepository.findByUserId(1L)).thenReturn(Collections.emptyList());
        List<ListDTO> result = service.getAllListsByUserId(1L);
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetListById_NotFound() {
        when(listRepository.findById(2L)).thenReturn(Optional.empty());
        Optional<ListDTO> result = service.getListById(2L, 1L);
        assertTrue(result.isEmpty());
    }
}
