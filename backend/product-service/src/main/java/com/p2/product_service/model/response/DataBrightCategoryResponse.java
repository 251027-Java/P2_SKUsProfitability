package com.p2.product_service.model.response;

import lombok.Data;

import java.util.List;

@Data
public class DataBrightCategoryResponse {

    private List<DataBrightSkuResponse> skuResponseList;
}
