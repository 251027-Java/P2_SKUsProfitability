package com.p2.product_service.controller;

import java.util.List;

import com.p2.product_service.repository.SKURepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.p2.product_service.dto.SKUDTO;
import com.p2.product_service.model.SKU;
import com.p2.product_service.model.request.BrightData.BrightDataCollectByUrlRequest;
import com.p2.product_service.model.request.BrightData.BrightDataDiscoverByBestSellerRequest;
import com.p2.product_service.service.SKUService;

import jakarta.servlet.http.HttpServletRequest;

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

    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    private boolean isAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("userRole");
        return "ADMIN".equalsIgnoreCase(role);
    }

    @GetMapping
    public ResponseEntity<List<SKUDTO>> getAllSKUs(HttpServletRequest request) {
        if (getUserId(request) == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        List<SKUDTO> skus = skuService.getAllSKUs();
        return ResponseEntity.ok(skus);
    }

    @GetMapping("/{skuId}")
    public ResponseEntity<SKUDTO> getSKU(@PathVariable Long skuId, HttpServletRequest request) {
        if (getUserId(request) == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return skuService.getSKUById(skuId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    public ResponseEntity<SKUDTO> searchSKU(@RequestParam String q, HttpServletRequest request) {
        if (getUserId(request) == null)
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        return skuService.searchBySku(q)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{skuId}")
    public ResponseEntity<Void> deleteSKU(@PathVariable Long skuId, HttpServletRequest request) {
        if (!isAdmin(request))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

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
        if (!isAdmin(request)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Admin access required");
        }

        SKU product = skuRpo.findBySku(sku)
                .orElseThrow(() -> new RuntimeException("Product not found: " + sku));

        try {
            String jsonProduct = objectMapper.writeValueAsString(product);

            producerService.sendMessage("sku-updates", jsonProduct);

            return ResponseEntity.ok("Successfully synced " + sku);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Sync failed: " + e.getMessage());
        }
    }

    @PostMapping("/add-category")
    public ResponseEntity<?> addProductsByCategory(@RequestBody BrightDataDiscoverByBestSellerRequest request){
        skuService.addSkusByCategory(request);

        return ResponseEntity.ok("");
    }

    @GetMapping("/get-all")
    public ResponseEntity<?> getProductDetails(@RequestBody BrightDataCollectByUrlRequest request) {
        List<SKU> skus = skuService.getSKUs();
        return ResponseEntity.ok(skus);
    }
}