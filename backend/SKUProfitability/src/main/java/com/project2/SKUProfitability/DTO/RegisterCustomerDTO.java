package com.project2.SKUProfitability.DTO;


public record RegisterCustomerDTO(
        String email,
        String password,
        String firstName,
        String lastName
) {}
