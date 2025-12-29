package com.project2.SKUProfitability.Controller;

import com.project2.SKUProfitability.Client.AuthServiceClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    public record AuthRequest(String email, String password){}
    public record RegisterRequest(String email, String password, String firstName, String lastName){}

    private final AuthServiceClient authServiceClient;

    public AuthController(AuthServiceClient authServiceClient) {
        this.authServiceClient = authServiceClient;
    }

    @PostMapping("/login")
    public AuthServiceClient.AuthResponse login(@RequestBody AuthRequest request) {
        try {
            return authServiceClient.login(request.email(), request.password());
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Login failed: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public AuthServiceClient.AuthResponse register(@RequestBody RegisterRequest request) {
        try {
            return authServiceClient.register(
                    request.email(),
                    request.password(),
                    request.firstName(),
                    request.lastName()
            );
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Registration failed: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public String health() {
        boolean authServiceHealthy = authServiceClient.isServiceHealthy();
        return "Auth Service: " + (authServiceHealthy ? "UP" : "DOWN");
    }
}
