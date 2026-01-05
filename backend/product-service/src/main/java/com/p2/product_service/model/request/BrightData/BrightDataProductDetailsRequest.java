package com.p2.product_service.model.request.BrightData;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class BrightDataProductDetailsRequest {
    private String url;
    private String zipcode;
    private String language;

}
