package com.p2.product_service.dto;

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
