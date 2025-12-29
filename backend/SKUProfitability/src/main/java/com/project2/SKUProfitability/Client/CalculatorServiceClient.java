package com.project2.SKUProfitability.Client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Client for communicating with the CalculatorService microservice.
 * Follows the pattern from the reference project's listener-service calling history-service.
 */
@Service
public class CalculatorServiceClient {
    private final RestTemplate restTemplate;

    @Value("${calculator.service.url:http://localhost:8081}")
    private String calculatorServiceUrl;

    public CalculatorServiceClient() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Calls the CalculatorService microservice to perform fee calculations.
     * Falls back to error response if the service is unavailable.
     *
     * @param request The calculation request
     * @return Response containing calculated fees and profits
     */
    public ResponseEntity<Map<String, Object>> calculateFees(FeeCalculationRequest request) {
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    calculatorServiceUrl + "/api/calculator/calculate",
                    request,
                    Map.class
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> body = response.getBody();
            return ResponseEntity.status(response.getStatusCode()).body(body);

        } catch (HttpClientErrorException.BadRequest badRequest) {
            // Return the error from the microservice
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid calculation parameters"));

        } catch (HttpClientErrorException.NotFound notFound) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Calculator service endpoint not found"));

        } catch (Exception e) {
            // Log error and return service unavailable
            System.err.println("Error calling CalculatorService: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body(Map.of(
                            "error", "Calculator service is currently unavailable",
                            "message", "Please try again later. The calculation service may be temporarily down."
                    ));
        }
    }

    /**
     * Health check for the CalculatorService.
     *
     * @return true if the service is reachable, false otherwise
     */
    public boolean isServiceHealthy() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    calculatorServiceUrl + "/api/calculator/health",
                    Map.class
            );
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("CalculatorService health check failed: " + e.getMessage());
            return false;
        }
    }

    /**
     * DTO for calculation requests - mirrors the microservice's DTO
     */
    public record FeeCalculationRequest(
            Long userId,
            BigDecimal length,
            BigDecimal width,
            BigDecimal height,
            BigDecimal weight,
            String category,
            BigDecimal sellingPrice,
            BigDecimal timeInStorage,
            BigDecimal freightCost,
            BigDecimal freightCostUnit,
            BigDecimal otherCosts,
            String otherCostsType,
            String fbaFeeCategory,
            BigDecimal referralFeePercentage
    ) {}
}
