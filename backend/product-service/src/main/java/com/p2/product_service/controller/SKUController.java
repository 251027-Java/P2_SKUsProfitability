package com.p2.product_service.controller;

import com.p2.product_service.dto.SKUDTO;
import com.p2.product_service.model.SKU;
import com.p2.product_service.model.request.DataBrightRequest;
import com.p2.product_service.service.SKUService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skus")
public class SKUController {

    private final SKUService skuService;
    private final com.p2.product_service.service.kafka.ProducerService producerService;

    public SKUController(SKUService skuService, com.p2.product_service.service.kafka.ProducerService producerService) {
        this.skuService = skuService;
        this.producerService = producerService;
    }

    // --- Security Helpers ---

    private Long getUserId(HttpServletRequest request) {
        return (Long) request.getAttribute("userId");
    }

    private boolean isAdmin(HttpServletRequest request) {
        String role = (String) request.getAttribute("userRole");
        return "ADMIN".equalsIgnoreCase(role);
    }

//    // Testing
//    private Long getUserId(HttpServletRequest request) {
//        // Hardcode to 1L so the controller thinks a user is logged in
//        return 1L;
//    }
//
//    private boolean isAdmin(HttpServletRequest request) {
//        // Always act like an admin for now
//        return true;
//    }

    // --- Public/Shared Endpoints (Requires valid login, any role) ---

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

    @GetMapping()
    public ResponseEntity<?> getProductDetails(@RequestBody DataBrightRequest request){
        List<SKU> skus = skuService.getSKUs();
        return ResponseEntity.ok(skus);
    }
}