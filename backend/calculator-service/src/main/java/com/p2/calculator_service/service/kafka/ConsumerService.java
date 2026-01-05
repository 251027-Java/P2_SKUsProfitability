package com.p2.calculator_service.service.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
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
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Product productFromKafka = objectMapper.readValue(message, Product.class);

            System.out.println("Received sync for SKU: " + productFromKafka.getSku());

            productRepository.save(productFromKafka);

            System.out.println("Successfully saved local copy: " + productFromKafka.getSku());

        } catch (Exception e) {
            System.err.println("Failed to parse or save product: " + e.getMessage());
        }
    }
}
