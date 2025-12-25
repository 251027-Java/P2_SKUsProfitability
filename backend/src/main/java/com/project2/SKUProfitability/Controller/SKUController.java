package com.project2.SKUProfitability.Controller;

import com.project2.SKUProfitability.DTO.SKUCreateDTO;
import com.project2.SKUProfitability.DTO.SKUDTO;
import com.project2.SKUProfitability.JwtUtil;
import com.project2.SKUProfitability.Service.SKUService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/skus")
public class SKUController {
    
    private final SKUService skuService;
    private final JwtUtil jwtUtil;
    
    public SKUController(SKUService skuService, JwtUtil jwtUtil) {
        this.skuService = skuService;
        this.jwtUtil = jwtUtil;
    }
    
    private Long getUserIdFromRequest(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            return jwtUtil.getUserIdFromToken(token);
        }
        throw new IllegalArgumentException("Invalid or missing authentication token");
    }
    
    @GetMapping
    public ResponseEntity<List<SKUDTO>> getAllSKUs(HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            List<SKUDTO> skus = skuService.getAllSKUsByUserId(userId);
            return ResponseEntity.ok(skus);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
    @GetMapping("/{skuId}")
    public ResponseEntity<SKUDTO> getSKU(@PathVariable Long skuId, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            Optional<SKUDTO> sku = skuService.getSKUById(skuId, userId);
            return sku.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<SKUDTO> searchSKU(
            @RequestParam String q,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            Optional<SKUDTO> sku = skuService.searchBySku(userId, q);
            return sku.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
    @PostMapping
    public ResponseEntity<SKUDTO> createSKU(@RequestBody SKUCreateDTO dto, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            SKUDTO createdSKU = skuService.createSKU(userId, dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSKU);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Object>> bulkCreateSKUs(
            @RequestBody List<SKUCreateDTO> skus,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            
            if (skus == null || skus.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "SKUs list cannot be empty"));
            }
            
            List<SKUDTO> createdSKUs = new ArrayList<>();
            List<String> errors = new ArrayList<>();
            
            for (int i = 0; i < skus.size(); i++) {
                try {
                    SKUDTO created = skuService.createSKU(userId, skus.get(i));
                    createdSKUs.add(created);
                } catch (Exception e) {
                    errors.add("SKU " + (i + 1) + " (" + skus.get(i).sku() + "): " + e.getMessage());
                }
            }
            
            return ResponseEntity.ok(Map.of(
                    "message", "Processed " + skus.size() + " SKU(s)",
                    "created", createdSKUs.size(),
                    "failed", errors.size(),
                    "skus", createdSKUs,
                    "errors", errors
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importFromCSV(
            @RequestParam("file") MultipartFile file,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            
            if (file.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "File is empty"));
            }
            
            if (!file.getContentType().equals("text/csv") && 
                !file.getOriginalFilename().endsWith(".csv")) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "File must be a CSV file"));
            }
            
            List<SKUDTO> importedSKUs = skuService.importFromCSV(userId, file);
            
            return ResponseEntity.ok(Map.of(
                    "message", "Successfully imported " + importedSKUs.size() + " SKUs",
                    "count", importedSKUs.size(),
                    "skus", importedSKUs
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PutMapping("/{skuId}")
    public ResponseEntity<SKUDTO> updateSKU(
            @PathVariable Long skuId,
            @RequestBody SKUCreateDTO dto,
            HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            SKUDTO updatedSKU = skuService.updateSKU(skuId, userId, dto);
            return ResponseEntity.ok(updatedSKU);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
    @DeleteMapping("/{skuId}")
    public ResponseEntity<Void> deleteSKU(@PathVariable Long skuId, HttpServletRequest request) {
        try {
            Long userId = getUserIdFromRequest(request);
            skuService.deleteSKU(skuId, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
