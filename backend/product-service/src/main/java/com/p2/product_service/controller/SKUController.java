package com.p2.product_service.controller;

import com.p2.product_service.dto.SKUCreateDTO;
import com.p2.product_service.dto.SKUDTO;
import com.p2.product_service.service.SKUService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/skus")
public class SKUController {

    private final SKUService skuService;
    private final com.p2.product_service.service.kafka.ProducerService producerService;

    public SKUController(SKUService skuService, com.p2.product_service.service.kafka.ProducerService producerService) {
        this.skuService = skuService;
        this.producerService = producerService;
    }

    private Long getUserId(HttpServletRequest request) {
        return 1L;
    }

    private boolean isAdmin(HttpServletRequest request) {
        return true;
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

    @PostMapping
    public ResponseEntity<SKUDTO> createSKU(@RequestBody SKUCreateDTO dto, HttpServletRequest request) {
        if (!isAdmin(request))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

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

            // Trigger Kafka for each imported SKU
            for (SKUDTO sku : importedSKUs) {
                producerService.sendMessage("sku-updates", sku.sku());
            }

            return ResponseEntity.ok(Map.of(
                    "message", "Successfully imported " + importedSKUs.size() + " SKUs",
                    "skus", importedSKUs));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
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
}