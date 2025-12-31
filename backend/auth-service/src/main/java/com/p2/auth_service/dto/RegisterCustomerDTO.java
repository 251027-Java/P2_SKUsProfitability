package com.p2.auth_service.dto;

public record RegisterCustomerDTO(
        String email,
        String password,
        String firstName,
        String lastName
) {}
