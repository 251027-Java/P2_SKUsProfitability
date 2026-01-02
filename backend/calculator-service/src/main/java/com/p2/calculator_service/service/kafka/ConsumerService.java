package com.p2.calculator_service.service.kafka;

import com.p2.calculator_service.model.Calculation;
import com.p2.calculator_service.model.Product;
import com.p2.calculator_service.repository.CalculationRepository;
import com.p2.calculator_service.repository.ProductRepository;
import com.p2.calculator_service.service.FBAFeeCalculatorService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ConsumerService {

    private final ProductRepository productRepository;
    private final CalculationRepository calculationRepository;
    private final FBAFeeCalculatorService calculatorService;

    public ConsumerService(ProductRepository productRepository,
            CalculationRepository calculationRepository,
            FBAFeeCalculatorService calculatorService) {
        this.productRepository = productRepository;
        this.calculationRepository = calculationRepository;
        this.calculatorService = calculatorService;
    }

    @KafkaListener(topics = "sku-updates", groupId = "calculator-group")
    public void listen(String message) {
        System.out.println("Calculator Service received message for SKU: " + message);

        try {
            // Assume message is the SKU string
            String sku = message.trim();

            // 1. Fetch Product Data
            Optional<Product> productOpt = productRepository.findBySku(sku);

            if (productOpt.isPresent()) {
                Product product = productOpt.get();
                System.out.println("Found product: " + product.getName());

                // 2. Run Calculation
                Calculation calculation = calculatorService.calculateForProduct(product);

                if (calculation != null) {
                    // Set User ID (placeholder or logic needed if part of message)
                    // For now, setting to a system default or handling null in DB
                    calculation.setUserId(1L); // Default system user

                    // 3. Save Results
                    calculationRepository.save(calculation);
                    System.out.println("Saved calculation for SKU: " + sku);
                }
            } else {
                System.out.println("Product not found for SKU: " + sku);
            }
        } catch (Exception e) {
            System.err.println("Error processing message: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
