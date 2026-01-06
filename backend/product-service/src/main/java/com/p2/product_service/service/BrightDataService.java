package com.p2.product_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.p2.product_service.exception.BrightDataException;
import com.p2.product_service.model.request.BrightData.BrightDataDiscoverByBestSellerRequest;
import com.p2.product_service.model.request.BrightData.SnapshotResponse;
import com.p2.product_service.model.response.BrightDataCategoryResponse;
import com.p2.product_service.model.response.BrightDataSkuResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

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

    public BrightDataCategoryResponse getBestSellersByCategory(BrightDataDiscoverByBestSellerRequest request) throws RuntimeException {

        try {
            //converting request object to json string
            String requestBody = mapper.writeValueAsString(request);

            //sending request to bright data
            HttpRequest brightDataRequest = HttpRequest.newBuilder()
                    .uri(new URI(brightDataCategoryUrl))
                    .header("Authorization", "Bearer " + apiKey)
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            //sending request to bright data
            HttpResponse<String> response = httpClient.send(brightDataRequest, HttpResponse.BodyHandlers.ofString());

            //converting response to json
            JsonNode root = mapper.readTree(response.body());

            // check if response contain data directly (not snapshot)
            if(!root.has("snapshot_id")){
                // If no snapshot_id, parse and return the response data directly
                return mapper.readValue(response.body(), BrightDataCategoryResponse.class);
            }

            //If we get here, the data isn't ready yet (snapshot is being prepared)
            log.info("DataBright Response not ready yet. Calling Snapshot API.");

            // Parse the snapshot ID from the response
            SnapshotResponse snapshotResponse = mapper.readValue(response.body(), SnapshotResponse.class);
            log.info("Snapshot response is: " +  snapshotResponse.toString());

            //Start polling the snapshot until data is ready
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

    private BrightDataCategoryResponse pollSnapshotUntilComplete(String snapshotId) throws RuntimeException {

        //Configure polling behavior
        int maxAttempts = 24;          // 24 * 10s = 240 seconds
        int pollIntervalMs = 10_000;   // 10 seconds

        try{
            //Start polling loop
            for (int attempt = 1; attempt <= maxAttempts; attempt++) {
                //Log current polling attempt
                log.info("Polling Snapshot API " +  attempt + "/" + maxAttempts);

                //Make API call to check snapshot status
                HttpResponse<String> snapshotResponse = callSnapshotApi(snapshotId);

                //Parse response as JSON
                JsonNode root = mapper.readTree(snapshotResponse.body());

                //Check if data is still being prepared
                if (root.has("status")) {

                    //Not ready yet → wait before next poll
                    Thread.sleep(pollIntervalMs);
                    continue; // Skip to next iteration
                }

                BrightDataCategoryResponse brightDataCategoryResponse = new BrightDataCategoryResponse();
                List<BrightDataSkuResponse> brightDataSkuResponseList = new ArrayList<>();

                try (BufferedReader reader = new BufferedReader(
                        new StringReader(snapshotResponse.body()))) {

                    String line;
                    while ((line = reader.readLine()) != null) {

                        JsonNode node = mapper.readTree(line);

                        //Filter out the error objects
                        if (!node.has("root_bs_category")) {
                            continue; // skip
                        }

                        // Map valid object
                        brightDataSkuResponseList.add(mapper.treeToValue(node, BrightDataSkuResponse.class));
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                brightDataCategoryResponse.setSkuResponseList(brightDataSkuResponseList);

                //Data is ready → return response
                return brightDataCategoryResponse;
            }

            //Data never became ready → throw exception
            throw new BrightDataException("Snapshot wasn't ready after " + maxAttempts + " attempts.");
        }
        catch (JsonProcessingException | InterruptedException e) {
            //Handle any errors during polling
            throw new BrightDataException(e.getMessage());
        }
    }

    private HttpResponse<String> callSnapshotApi(String snapshotId) {
        try{
            // Create a new HTTP request using the Builder pattern
            HttpRequest request = HttpRequest.newBuilder()
                    // Set the target URL by combining base URL and snapshot ID
                    .uri(URI.create(brightDataSnapshotUrl + snapshotId))
                    // Set request timeout to 30 seconds
                    .timeout(Duration.ofSeconds(30))
                    // Add Authorization header with Bearer token
                    .header("Authorization", "Bearer " + apiKey)
                    // Specify this is a GET request
                    .GET()
                    // Build the request
                    .build();

            // Send the request using the httpClient and return the response
            // The response body will be handled as a String
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        }
        // Catch block for handling I/O related errors
        catch (IOException | InterruptedException e){
            log.error("Error calling snapshot api");
            return null;
        }
    }
}
