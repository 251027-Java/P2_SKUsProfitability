package com.skuProfitability.producer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@SpringBootApplication
@RestController
@RequestMapping("/api/messages")
public class ProducerApplication {

	private final ProducerService producerService;
	private static final String TOPIC = "messages";

	public ProducerApplication(ProducerService producerService) {
		this.producerService = producerService;
	}

	public static void main(String[] args) {
		SpringApplication.run(ProducerApplication.class, args);
	}

	@PostMapping
	public Map<String, String> sendMessage(@RequestBody Map<String, String> payload) {
		String message = payload.get("message");

		producerService.sendMessage(TOPIC, message);

		System.out.println("Sending message: " + message);

		return Map.of("status", "sent", "topic", TOPIC, "message", message);
	}

	@GetMapping("/health")
	public Map<String, String> health() {
		return Map.of("status", "UP", "service", "producer");
	}
}
