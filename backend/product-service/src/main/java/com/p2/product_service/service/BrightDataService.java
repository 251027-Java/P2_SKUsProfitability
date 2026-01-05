package com.p2.product_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.p2.product_service.exception.BrightDataException;
import com.p2.product_service.model.request.BrightData.BrightDataDiscoverByBestSellerRequest;
import com.p2.product_service.model.request.BrightData.SnapshotResponse;
import com.p2.product_service.model.response.DataBrightCategoryResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class BrightDataService {

    private final HttpClient httpClient;
    private final ObjectMapper mapper;

    @Value("${brightdata.api.category.url}")
    private String brightDataCategoryUrl;

    @Value("${brightdata.api.snapshot.url}")
    private String brightDataSnapshotUrl;

    @Value("${brightdata.api.key}")
    private String apiKey;

    public DataBrightCategoryResponse getBestSellersByCategory(BrightDataDiscoverByBestSellerRequest request) throws RuntimeException {

        try {
            String requestBody = mapper.writeValueAsString(request);
            HttpRequest brightDataRequest = HttpRequest.newBuilder()
                    .uri(new URI(brightDataCategoryUrl))
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(brightDataRequest, HttpResponse.BodyHandlers.ofString());
            JsonNode root = mapper.readTree(response.body());
            if(!root.has("snapshot_id")){
                return mapper.readValue(response.body(), DataBrightCategoryResponse.class);
            }
            log.info("DataBright Response not ready yet. Calling Snapshot API.");
            SnapshotResponse snapshotResponse = mapper.readValue(response.body(), SnapshotResponse.class);
            log.info("Snapshot response is: " +  snapshotResponse.toString());
            return pollSnapshotUntilComplete(snapshotResponse.getSnapshotId());
        } catch (InterruptedException e) {
            log.error("Error sending request to bright data: " + e);
            return null;
        } catch (URISyntaxException e) {
            log.error("Invalid URI: " + e);
            return null;
        } catch (IOException e) {
            log.error("Error parsing object: " + e);
            return null;
        }
    }

    private DataBrightCategoryResponse pollSnapshotUntilComplete(String snapshotId) throws RuntimeException {

        int maxAttempts = 24;          // 24 * 10s = 240 seconds
        int pollIntervalMs = 10_000;   // 10 seconds

        try{
            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                log.info("Polling Snapshot API " +  attempt + "/" + maxAttempts);
                HttpResponse<String> snapshotResponse =
                        callSnapshotApi(snapshotId);
                JsonNode root = mapper.readTree(snapshotResponse.body());

                if (root.has("status")) {
                    // Not ready yet → wait before next poll
                    Thread.sleep(pollIntervalMs);
                    continue;
                }
                return mapper.readValue(snapshotResponse.body(), DataBrightCategoryResponse.class);
            }
            throw new BrightDataException("Snapshot wasn't ready after " + maxAttempts + " attempts.");
        }
        catch (JsonProcessingException | InterruptedException e) {
            throw new BrightDataException(e.getMessage());
        }
    }

    private HttpResponse<String> callSnapshotApi(String snapshotId) {
        try{
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(brightDataSnapshotUrl + snapshotId))
                    .timeout(Duration.ofSeconds(30))
                    .header("Authorization", "Bearer " + apiKey)
                    .GET()
                    .build();

            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        }
        catch (IOException | InterruptedException e){
            log.error("Error calling snapshot api");
            return null;
        }
    }
}
