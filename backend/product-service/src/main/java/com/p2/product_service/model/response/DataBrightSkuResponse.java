package com.p2.product_service.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class DataBrightSkuResponse {
    private String asin;
    private String title;
    private String description;
    private String imageUrl;
    private double finalPrice;
    private double itemWeight;
    private String productDimensions;
    private double length;
    private double width;
    private double height;
    private String rootBsCategory;
}
