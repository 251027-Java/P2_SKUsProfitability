package com.ServiceTest;

import com.p2.auth_service.dto.AppUserDTO;
import com.p2.auth_service.dto.RegisterCustomerDTO;
import com.p2.auth_service.model.AppUser;
import com.p2.auth_service.repository.AppUserRepository;
import com.p2.auth_service.service.AppUserService;
import com.p2.auth_service.util.ValidationUtil;
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

    private RegisterCustomerDTO validDto;
    private AppUser savedUser;

    @BeforeEach
    void setUp() {
        validDto = new RegisterCustomerDTO("test@email.com", "Password123!", "John", "Doe");
        savedUser = new AppUser("test@email.com", "hashedPassword", "SELLER", "John", "Doe");
        savedUser.setUserId(1L);
    }

    @Test
    void registerNewCustomer_HappyPath() {
        when(repository.findByEmail(validDto.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(validDto.password())).thenReturn("hashedPassword");
        when(repository.save(any(AppUser.class))).thenReturn(savedUser);
        AppUserDTO result = service.registerNewCustomer(validDto);
        assertNotNull(result);
        assertEquals("test@email.com", result.email());
        assertEquals("SELLER", result.userRole());
        assertEquals("John", result.firstName());
        assertEquals("Doe", result.lastName());
        assertEquals(1L, result.userId());
    }

    @Test
    void registerNewCustomer_EmailAlreadyExists_ThrowsException() {
        when(repository.findByEmail(validDto.email())).thenReturn(Optional.of(savedUser));
        Exception exception = assertThrows(IllegalArgumentException.class, () -> service.registerNewCustomer(validDto));
        assertEquals("Email already in use.", exception.getMessage());
    }

    @Test
    void appUserToDto_MapsCorrectly() {
        // Same stubbing as happy path to ensure mapping happens
        when(repository.findByEmail(validDto.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(validDto.password())).thenReturn("hashedPassword");
        when(repository.save(any(AppUser.class))).thenReturn(savedUser);

        AppUserDTO dto = service.registerNewCustomer(validDto);
        assertEquals(savedUser.getEmail(), dto.email());
        assertEquals(savedUser.getUserRole(), dto.userRole());
        assertEquals(savedUser.getFirstName(), dto.firstName());
        assertEquals(savedUser.getLastName(), dto.lastName());
    }

    @Test
    void registerNewCustomer_InvalidEmail_ThrowsException() {
        RegisterCustomerDTO invalidEmailDto = new RegisterCustomerDTO("bademail", "Password123!", "John", "Doe");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> service.registerNewCustomer(invalidEmailDto));
        assertTrue(exception.getMessage().contains("Invalid email format"));
    }

    @Test
    void registerNewCustomer_InvalidPassword_ThrowsException() {
        RegisterCustomerDTO invalidPasswordDto = new RegisterCustomerDTO("test@email.com", "bad", "John", "Doe");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> service.registerNewCustomer(invalidPasswordDto));
        assertTrue(exception.getMessage().contains("at least 6"));
    }

    @Test
    void registerNewCustomer_InvalidFirstName_ThrowsException() {
        RegisterCustomerDTO invalidFirstNameDto = new RegisterCustomerDTO("test@email.com", "Password123!", "", "Doe");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> service.registerNewCustomer(invalidFirstNameDto));
        assertTrue(exception.getMessage().contains("First Name is required"));
    }

    @Test
    void registerNewCustomer_InvalidLastName_ThrowsException() {
        RegisterCustomerDTO invalidLastNameDto = new RegisterCustomerDTO("test@email.com", "Password123!", "John", "");
        Exception exception = assertThrows(IllegalArgumentException.class, () -> service.registerNewCustomer(invalidLastNameDto));
        assertTrue(exception.getMessage().contains("Last Name is required"));
    }
}
