package com.project2.SKUProfitability.DTO;

/*
@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "userRole", nullable = false)
    private String userRole;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;
*/

public record AppUserDTO (
        Long userId,
        String email,
        String password,
        String userRole,
        String firstName,
        String lastName
) {}
