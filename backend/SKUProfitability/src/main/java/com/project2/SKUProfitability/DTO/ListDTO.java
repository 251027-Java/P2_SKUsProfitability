package com.project2.SKUProfitability.DTO;

import java.time.LocalDateTime;
import java.util.List;

public record ListDTO(
        Long listId,
        Long userId,
        String name,
        String description,
        Integer itemCount,
        List<SKUDTO> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}

