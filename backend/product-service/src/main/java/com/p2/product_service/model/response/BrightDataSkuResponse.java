package com.p2.product_service.model.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class BrightDataSkuResponse {
    private String asin;
    private String title;
    private String description;
    private String imageUrl;
    private double finalPrice;
    private String itemWeight;
    private String productDimensions;
    private double length;
    private double width;
    private double height;
    private String rootBsCategory;
}
