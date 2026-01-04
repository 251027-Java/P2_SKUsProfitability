package com.p2.product_service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.p2.product_service.dto.SKUCreateDTO;
import com.p2.product_service.dto.SKUDTO;
import com.p2.product_service.model.SKU;
import com.p2.product_service.repository.SKURepository;
import com.p2.product_service.service.SKUService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

import static org.springframework.kafka.support.KafkaHeaders.TOPIC;

@RestController
@RequestMapping("/api/skus")
public class SKUController {

    private final SKUService skuService;
    private final com.p2.product_service.service.kafka.ProducerService producerService;
    private final SKURepository skuRpo;
    private final ObjectMapper objectMapper;

    public SKUController(SKUService skuService, com.p2.product_service.service.kafka.ProducerService producerService, SKURepository skuRpo, ObjectMapper objectMapper) {
        this.skuService = skuService;
        this.producerService = producerService;
        this.skuRpo = skuRpo;
        this.objectMapper = objectMapper;
    }

    // --- Security Helpers ---

//    private Long getUserId(HttpServletRequest request) {
//        return (Long) request.getAttribute("userId");
//    }
//
//    private boolean isAdmin(HttpServletRequest request) {
//        String role = (String) request.getAttribute("userRole");
//        return "ADMIN".equalsIgnoreCase(role);
//    }

    // Testing
    private Long getUserId(HttpServletRequest request) {
        // Hardcode to 1L so the controller thinks a user is logged in
        return 1L;
    }

    private boolean isAdmin(HttpServletRequest request) {
        // Always act like an admin for now
        return true;
    }

    // --- Public/Shared Endpoints (Requires valid login, any role) ---

    @GetMapping
    public ResponseEntity<List<SKUDTO>> getAllSKUs(HttpServletRequest request) {
        if (getUserId(request) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<SKUDTO> skus = skuService.getAllSKUs();
        return ResponseEntity.ok(skus);
    }

    @GetMapping("/{skuId}")
    public ResponseEntity<SKUDTO> getSKU(@PathVariable Long skuId, HttpServletRequest request) {
        if (getUserId(request) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return skuService.getSKUById(skuId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<SKUDTO> searchSKU(@RequestParam String q, HttpServletRequest request) {
        if (getUserId(request) == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return skuService.searchBySku(q)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- Administrative Endpoints (Strictly ADMIN only) ---

    @PostMapping
    public ResponseEntity<SKUDTO> createSKU(@RequestBody SKUCreateDTO dto, HttpServletRequest request) {
        if (!isAdmin(request)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        try {
            SKUDTO createdSKU = skuService.createSKU(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSKU);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importFromCSV(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {

        if (!isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Access Denied: Admin role required"));
        }

        if (file.isEmpty() || !file.getOriginalFilename().endsWith(".csv")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Valid CSV file required"));
        }

        try {
            List<SKUDTO> importedSKUs = skuService.importFromCSV(getUserId(request), file);
            return ResponseEntity.ok(Map.of(
                    "message", "Successfully imported " + importedSKUs.size() + " SKUs",
                    "skus", importedSKUs
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{skuId}")
    public ResponseEntity<Void> deleteSKU(@PathVariable Long skuId, HttpServletRequest request) {
        if (!isAdmin(request)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        try {
            skuService.deleteSKU(skuId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/test-kafka")
    public ResponseEntity<String> testKafka(@RequestParam String sku, HttpServletRequest request) {
        if (!isAdmin(request))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        producerService.sendMessage("sku-updates", sku);
        return ResponseEntity.ok("Sent Kafka message for SKU: " + sku);
    }

    @PostMapping("/sync")
    public ResponseEntity<String> syncProduct(@RequestParam("sku") String sku, HttpServletRequest request) {
        // Keep it consistent with your other endpoints
        if (!isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }

        // 1. Find the product
        SKU product = skuRpo.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("Product not found: " + sku));

        try {
            // 2. Convert to JSON (using the injected, TimeModule-capable mapper)
            String jsonProduct = objectMapper.writeValueAsString(product);

            // 3. Send to Kafka
            producerService.sendMessage("sku-updates", jsonProduct);

            return ResponseEntity.ok("Successfully synced " + sku);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Sync failed: " + e.getMessage());
        }
    }

//    @PostMapping("/sync-product/{sku}")
//    public ResponseEntity<String> syncProduct(@PathVariable String sku) {
//        // 1. Get the full product object from the DB
//        SKU product = skuRpo.findBySku(sku)
//                .orElseThrow(() -> new RuntimeException("Product not found"));
//
//        try {
//            // 2. Convert the Object into a JSON String manually
//            ObjectMapper objectMapper = new ObjectMapper();
//            String jsonProduct = objectMapper.writeValueAsString(product);
//
//            // 3. Send that JSON String to your partner's existing method
//            producerService.sendMessage("sku-updates", jsonProduct);
//
//            return ResponseEntity.ok("Full product data sent as JSON for SKU: " + sku);
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                    .body("Error converting product to JSON: " + e.getMessage());
//        }
//    }
}