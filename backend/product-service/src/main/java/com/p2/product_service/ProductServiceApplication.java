package com.p2.product_service;

import com.p2.product_service.model.SKU;
import com.p2.product_service.repository.SKURepository;
import com.p2.product_service.service.BasicCalculationService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ProductServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProductServiceApplication.class, args);
	}

	@Bean
	ApplicationListener<ApplicationReadyEvent> updateExistingSizeClassifications(
			SKURepository skuRepository, BasicCalculationService calculationService) {
		return event -> {
			try {
				var skusToUpdate = skuRepository.findAll().stream()
						.filter(sku -> sku.getLength() != null && sku.getWidth() != null &&
								sku.getHeight() != null && sku.getWeight() != null &&
								(sku.getSizeClassification() == null || sku.getSizeClassification().isEmpty()))
						.toList();

				if (!skusToUpdate.isEmpty()) {
					skusToUpdate.forEach(sku -> {
						String sizeClassification = calculationService.determineSizeTier(
								sku.getLength(), sku.getWidth(), sku.getHeight(), sku.getWeight());
						sku.setSizeClassification(sizeClassification);
					});
					skuRepository.saveAll(skusToUpdate);
				}
			} catch (Exception e) {
				System.err.println("Error updating existing size classifications: " + e.getMessage());
			}
		};
	}
}
