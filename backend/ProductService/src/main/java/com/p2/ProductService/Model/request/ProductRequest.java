package com.p2.ProductService.Model.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ProductRequest {
    private String url;
    private String zipcode;
    private String language;

}
