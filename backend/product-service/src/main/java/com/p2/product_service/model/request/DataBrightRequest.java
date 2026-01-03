package com.p2.product_service.model.request;

import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;

@Data
@NoArgsConstructor
public class DataBrightRequest {
     List<ProductDetails> input;
}
