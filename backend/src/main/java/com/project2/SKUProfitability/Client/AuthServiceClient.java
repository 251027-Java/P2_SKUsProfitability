package com.project2.SKUProfitability.Client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class AuthServiceClient {
    private final RestTemplate restTemplate;
    private final String authServiceUrl;

    public record LoginRequest(String email, String password) {}
    public record RegisterRequest(String email, String password, String firstName, String lastName) {}
    public record AuthResponse(String token) {}

    public AuthServiceClient(
            RestTemplate restTemplate,
            @Value("${auth.service.url:http://localhost:8081}") String authServiceUrl) {
        this.restTemplate = restTemplate;
        this.authServiceUrl = authServiceUrl;
    }

    public AuthResponse login(String email, String password) {
        String url = authServiceUrl + "/api/auth/login";
        
        LoginRequest request = new LoginRequest(email, password);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<LoginRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<AuthResponse> response = restTemplate.postForEntity(url, entity, AuthResponse.class);
            return response.getBody();
        } catch (ResourceAccessException e) {
            throw new RuntimeException("Cannot connect to auth-service at " + authServiceUrl + ". Please ensure auth-service is running on port 8081.", e);
        } catch (HttpClientErrorException e) {
            // Pass through HTTP client errors (4xx) with their original message
            throw new RuntimeException("Auth service error: " + e.getResponseBodyAsString(), e);
        } catch (HttpServerErrorException e) {
            // Pass through HTTP server errors (5xx) with their original message
            throw new RuntimeException("Auth service error: " + e.getResponseBodyAsString(), e);
        } catch (RestClientException e) {
            throw new RuntimeException("Error communicating with auth-service: " + e.getMessage(), e);
        }
    }

    public AuthResponse register(String email, String password, String firstName, String lastName) {
        String url = authServiceUrl + "/api/auth/register";
        
        RegisterRequest request = new RegisterRequest(email, password, firstName, lastName);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<RegisterRequest> entity = new HttpEntity<>(request, headers);

        try {
            ResponseEntity<AuthResponse> response = restTemplate.postForEntity(url, entity, AuthResponse.class);
            return response.getBody();
        } catch (ResourceAccessException e) {
            throw new RuntimeException("Cannot connect to auth-service at " + authServiceUrl + ". Please ensure auth-service is running on port 8081.", e);
        } catch (HttpClientErrorException e) {
            // Pass through HTTP client errors (4xx) with their original message
            throw new RuntimeException("Auth service error: " + e.getResponseBodyAsString(), e);
        } catch (HttpServerErrorException e) {
            // Pass through HTTP server errors (5xx) with their original message
            throw new RuntimeException("Auth service error: " + e.getResponseBodyAsString(), e);
        } catch (RestClientException e) {
            throw new RuntimeException("Error communicating with auth-service: " + e.getMessage(), e);
        }
    }

    public boolean isServiceHealthy() {
        try {
            String url = authServiceUrl + "/api/auth/health";
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }
}
