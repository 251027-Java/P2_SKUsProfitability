package com.p2.AuthService.dto;

public record RegisterCustomerDTO(
        String email,
        String password,
        String firstName,
        String lastName
) {}
