package com.p2.auth_service.controller;

import com.p2.auth_service.dto.AppUserDTO;
import com.p2.auth_service.dto.RegisterCustomerDTO;
import com.p2.auth_service.model.AppUser;
import com.p2.auth_service.repository.AppUserRepository;
import com.p2.auth_service.service.AppUserService;
import com.p2.auth_service.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    public record AuthRequest(String email, String password){}
    public record AuthResponse(String token){}

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AppUserService appUserService;

    public AuthController(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, AppUserService appUserService) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.appUserService = appUserService;
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody AuthRequest request) {
        Optional<AppUser> optionalUser = appUserRepository.findByEmail(request.email);
        if(optionalUser.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        AppUser user = optionalUser.get();
        Long userId = user.getUserId();

        if(!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid password");
        }

        String token = jwtUtil.generateToken(
                userId,
                user.getEmail(),
                user.getUserRole()
        );

        return new AuthResponse(token);
    }

    @PostMapping("/register")
    public AuthResponse register(@RequestBody RegisterCustomerDTO request) {
        try {
            AppUserDTO user = appUserService.registerNewCustomer(request);
            Long userId = user.userId();

            String token = jwtUtil.generateToken(
                    userId,
                    request.email(),
                    "SELLER"
            );

            return new AuthResponse(token);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Registration failed: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "AuthService");
    }
}
