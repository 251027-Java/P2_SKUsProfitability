package com.project2.SKUProfitability.Client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
public class ProductServiceClient {
    private final RestTemplate restTemplate;

    @Value("${product.service.url:http://localhost:8083}")
    private String productServiceUrl;

    public ProductServiceClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ResponseEntity<List<Map<String, Object>>> getAllSKUs(String authToken) {
        try {
            HttpHeaders headers = createAuthHeaders(authToken);
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<List> response = restTemplate.exchange(
                    productServiceUrl + "/api/skus",
                    HttpMethod.GET,
                    entity,
                    List.class
            );
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> body = response.getBody();
            return ResponseEntity.status(response.getStatusCode()).body(body);
        } catch (Exception e) {
            throw new RuntimeException("Product service unavailable", e);
        }
    }

    public ResponseEntity<Map<String, Object>> getSKU(Long skuId, String authToken) {
        try {
            HttpHeaders headers = createAuthHeaders(authToken);
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    productServiceUrl + "/api/skus/" + skuId,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> body = response.getBody();
            return ResponseEntity.status(response.getStatusCode()).body(body);
        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            throw new RuntimeException("Product service unavailable", e);
        }
    }

    public ResponseEntity<Map<String, Object>> searchSKU(String searchTerm, String authToken) {
        try {
            HttpHeaders headers = createAuthHeaders(authToken);
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    productServiceUrl + "/api/skus/search?q=" + searchTerm,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> body = response.getBody();
            return ResponseEntity.status(response.getStatusCode()).body(body);
        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            throw new RuntimeException("Product service unavailable", e);
        }
    }

    public ResponseEntity<Map<String, Object>> createSKU(Map<String, Object> skuData, String authToken) {
        try {
            HttpHeaders headers = createAuthHeaders(authToken);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(skuData, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    productServiceUrl + "/api/skus",
                    entity,
                    Map.class
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> body = response.getBody();
            return ResponseEntity.status(response.getStatusCode()).body(body);
        } catch (Exception e) {
            throw new RuntimeException("Product service unavailable", e);
        }
    }

    public ResponseEntity<Map<String, Object>> importFromCSV(MultipartFile file, String authToken) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            if (authToken != null && authToken.startsWith("Bearer ")) {
                headers.set("Authorization", authToken);
            } else if (authToken != null) {
                headers.set("Authorization", "Bearer " + authToken);
            }
            
            ByteArrayResource resource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };
            
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", resource);
            
            HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    productServiceUrl + "/api/skus/import",
                    entity,
                    Map.class
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> responseBody = response.getBody();
            return ResponseEntity.status(response.getStatusCode()).body(responseBody);
        } catch (Exception e) {
            throw new RuntimeException("Product service unavailable", e);
        }
    }

    public ResponseEntity<Void> deleteSKU(Long skuId, String authToken) {
        try {
            HttpHeaders headers = createAuthHeaders(authToken);
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            restTemplate.exchange(
                    productServiceUrl + "/api/skus/" + skuId,
                    HttpMethod.DELETE,
                    entity,
                    Void.class
            );
            
            return ResponseEntity.noContent().build();
        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            throw new RuntimeException("Product service unavailable", e);
        }
    }

    public ResponseEntity<List<Map<String, Object>>> getAllLists(String authToken) {
        try {
            HttpHeaders headers = createAuthHeaders(authToken);
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<List> response = restTemplate.exchange(
                    productServiceUrl + "/api/lists",
                    HttpMethod.GET,
                    entity,
                    List.class
            );
            
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> body = response.getBody();
            return ResponseEntity.status(response.getStatusCode()).body(body);
        } catch (Exception e) {
            throw new RuntimeException("Product service unavailable", e);
        }
    }

    public ResponseEntity<Map<String, Object>> getList(Long listId, String authToken) {
        try {
            HttpHeaders headers = createAuthHeaders(authToken);
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    productServiceUrl + "/api/lists/" + listId,
                    HttpMethod.GET,
                    entity,
                    Map.class
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> body = response.getBody();
            return ResponseEntity.status(response.getStatusCode()).body(body);
        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            throw new RuntimeException("Product service unavailable", e);
        }
    }

    public ResponseEntity<Map<String, Object>> createList(Map<String, Object> listData, String authToken) {
        try {
            HttpHeaders headers = createAuthHeaders(authToken);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(listData, headers);
            
            ResponseEntity<Map> response = restTemplate.postForEntity(
                    productServiceUrl + "/api/lists",
                    entity,
                    Map.class
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> body = response.getBody();
            return ResponseEntity.status(response.getStatusCode()).body(body);
        } catch (Exception e) {
            throw new RuntimeException("Product service unavailable", e);
        }
    }

    public ResponseEntity<Map<String, Object>> updateList(Long listId, Map<String, Object> listData, String authToken) {
        try {
            HttpHeaders headers = createAuthHeaders(authToken);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(listData, headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    productServiceUrl + "/api/lists/" + listId,
                    HttpMethod.PUT,
                    entity,
                    Map.class
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> body = response.getBody();
            return ResponseEntity.status(response.getStatusCode()).body(body);
        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            throw new RuntimeException("Product service unavailable", e);
        }
    }

    public ResponseEntity<Void> deleteList(Long listId, String authToken) {
        try {
            HttpHeaders headers = createAuthHeaders(authToken);
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            restTemplate.exchange(
                    productServiceUrl + "/api/lists/" + listId,
                    HttpMethod.DELETE,
                    entity,
                    Void.class
            );
            
            return ResponseEntity.noContent().build();
        } catch (HttpClientErrorException.NotFound e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            throw new RuntimeException("Product service unavailable", e);
        }
    }

    public ResponseEntity<Map<String, Object>> addSKUToList(Long listId, Long skuId, String authToken) {
        try {
            HttpHeaders headers = createAuthHeaders(authToken);
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    productServiceUrl + "/api/lists/" + listId + "/skus/" + skuId,
                    HttpMethod.POST,
                    entity,
                    Map.class
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> body = response.getBody();
            return ResponseEntity.status(response.getStatusCode()).body(body);
        } catch (Exception e) {
            throw new RuntimeException("Product service unavailable", e);
        }
    }

    public ResponseEntity<Map<String, Object>> removeSKUFromList(Long listId, Long skuId, String authToken) {
        try {
            HttpHeaders headers = createAuthHeaders(authToken);
            HttpEntity<?> entity = new HttpEntity<>(headers);
            
            ResponseEntity<Map> response = restTemplate.exchange(
                    productServiceUrl + "/api/lists/" + listId + "/skus/" + skuId,
                    HttpMethod.DELETE,
                    entity,
                    Map.class
            );
            
            @SuppressWarnings("unchecked")
            Map<String, Object> body = response.getBody();
            return ResponseEntity.status(response.getStatusCode()).body(body);
        } catch (Exception e) {
            throw new RuntimeException("Product service unavailable", e);
        }
    }

    public boolean isServiceHealthy() {
        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(
                    productServiceUrl + "/api/product/health",
                    Map.class
            );
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            System.err.println("ProductService health check failed: " + e.getMessage());
            return false;
        }
    }

    private HttpHeaders createAuthHeaders(String authToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (authToken != null && authToken.startsWith("Bearer ")) {
            headers.set("Authorization", authToken);
        } else if (authToken != null) {
            headers.set("Authorization", "Bearer " + authToken);
        }
        return headers;
    }
}

