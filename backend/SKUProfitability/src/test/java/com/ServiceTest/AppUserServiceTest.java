package com.ServiceTest;

import com.project2.SKUProfitability.DTO.AppUserDTO;
import com.project2.SKUProfitability.DTO.RegisterCustomerDTO;
import com.project2.SKUProfitability.Model.AppUser;
import com.project2.SKUProfitability.Repository.AppUserRepository;
import com.project2.SKUProfitability.Service.AppUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AppUserServiceTest {
    @Mock
    private AppUserRepository repository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private AppUserService service;

    private RegisterCustomerDTO dto;
    private AppUser user;

    @BeforeEach
    void setUp() {
        dto = new RegisterCustomerDTO("user@email.com", "pass123", "Jane", "Smith");
        user = new AppUser("user@email.com", "hashedPass", "SELLER", "Jane", "Smith");
        user.setUserId(2L);
    }

    @Test
    void testRegisterNewCustomer_EmailExists() {
        when(repository.findByEmail(dto.email())).thenReturn(Optional.of(user));
        assertThrows(IllegalArgumentException.class, () -> service.registerNewCustomer(dto));
    }

    @Test
    void testRegisterNewCustomer_Success() {
        when(repository.findByEmail(dto.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(dto.password())).thenReturn("hashedPass");
        when(repository.save(any(AppUser.class))).thenReturn(user);
        AppUserDTO result = service.registerNewCustomer(dto);
        assertEquals("user@email.com", result.email());
        assertEquals("Jane", result.firstName());
        assertEquals("Smith", result.lastName());
        assertEquals("SELLER", result.userRole());
    }

    @Test
    void testAppUserToDto() {
        AppUserDTO dto = service.registerNewCustomer(this.dto);
        assertNotNull(dto);
        assertEquals("user@email.com", dto.email());
    }
}
