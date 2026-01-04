package com.p2.product_service.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class DataBrightResponse {
    private String title;
    private String description;
    private String imageUrl;
    private double finalPrice;
    private double itemWeight;
    private String productDimension;
    private double length;
    private double width;
    private double height;
}
