package com.p2.ProductService.DTO;

import java.time.LocalDateTime;
import java.util.List;

public record SellerListDTO(
        Long listId,
        Long userId,
        String name,
        String description,
        Integer itemCount,
        List<SKUDTO> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
