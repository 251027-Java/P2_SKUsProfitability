package com.project2.SKUProfitability.Controller;

import com.project2.SKUProfitability.DTO.AppUserDTO;
import com.project2.SKUProfitability.DTO.RegisterCustomerDTO;
import com.project2.SKUProfitability.JwtUtil;
import com.project2.SKUProfitability.Model.AppUser;
import com.project2.SKUProfitability.Repository.AppUserRepository;
import com.project2.SKUProfitability.Service.AppUserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
}
