package com.project2.SKUProfitability.DTO;

public record AppUserDTO (
        Long userId,
        String email,
        String password,
        String userRole,
        String firstName,
        String lastName
) {}
