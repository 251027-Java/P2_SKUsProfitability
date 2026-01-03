package com.p2.product_service.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductDetails {
    private String url;
    private String zipcode;
    private String language;

}
