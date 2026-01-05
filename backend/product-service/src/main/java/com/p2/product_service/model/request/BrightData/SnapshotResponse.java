package com.p2.product_service.model.request.BrightData;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class SnapshotResponse {

    @JsonProperty("snapshot_id")
    private String snapshotId;
}
