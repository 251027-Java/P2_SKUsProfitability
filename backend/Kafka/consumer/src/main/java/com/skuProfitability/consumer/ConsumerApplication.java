package com.skuProfitability.consumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@SpringBootApplication
@RestController
@RequestMapping("/api/messages")
public class ConsumerApplication {

	private final ConsumerService consumerService;

	public ConsumerApplication(ConsumerService consumerService) {
		this.consumerService = consumerService;
	}

	public static void main(String[] args) {
		SpringApplication.run(ConsumerApplication.class, args);
	}

	@GetMapping
	public Map<String, Object> getMessages() {
		return Map.of(
				"count", consumerService.getMessages().size(),
				"messages", consumerService.getMessages());
	}

	@DeleteMapping
	public Map<String, String> clearMessages() {
		consumerService.clearMessages();
		return Map.of("status", "cleared");
	}

	@GetMapping("/health")
	public Map<String, String> health() {
		return Map.of("status", "UP", "service", "consumer");
	}
}
