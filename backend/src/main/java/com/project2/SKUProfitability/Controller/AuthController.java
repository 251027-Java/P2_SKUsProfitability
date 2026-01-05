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
        } catch (RuntimeException e) {
            String message = e.getMessage();
            if (message != null && message.contains("Cannot connect to auth-service")) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message);
            }
            if (message != null && (message.contains("User not found") || message.contains("Invalid password"))) {
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, message);
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Login failed: " + message);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Login failed: " + e.getMessage());
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
        } catch (RuntimeException e) {
            String message = e.getMessage();
            if (message != null && message.contains("Cannot connect to auth-service")) {
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, message);
            }
            if (message != null && message.contains("Email already in use")) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, message);
            }
            if (message != null && message.contains("Auth service error")) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
            }
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Registration failed: " + message);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Registration failed: " + e.getMessage());
        }
    }

    @GetMapping("/health")
    public String health() {
        boolean authServiceHealthy = authServiceClient.isServiceHealthy();
        return "Auth Service: " + (authServiceHealthy ? "UP" : "DOWN");
    }
}
