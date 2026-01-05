package com.p2.product_service.model.request.BrightData;

import lombok.Data;

import java.util.List;

@Data
public class BrightDataDiscoverByBestSellerRequest {

    private List<BrightDataBestSellerRequest> input;
}
