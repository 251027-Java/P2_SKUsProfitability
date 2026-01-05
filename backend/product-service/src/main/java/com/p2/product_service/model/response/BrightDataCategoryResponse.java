package com.p2.product_service.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BrightDataCategoryResponse {

    private List<BrightDataSkuResponse> skuResponseList;
}
