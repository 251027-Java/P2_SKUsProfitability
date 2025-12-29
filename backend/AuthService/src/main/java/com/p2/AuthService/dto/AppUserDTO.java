package com.p2.AuthService.dto;

public record AppUserDTO (
        Long userId,
        String email,
        String password,
        String userRole,
        String firstName,
        String lastName
) {}
