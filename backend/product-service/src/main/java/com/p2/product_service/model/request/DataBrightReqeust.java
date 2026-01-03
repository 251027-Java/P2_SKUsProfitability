package com.p2.ProductService.Model.request;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class DataBrightReqeust {
    List<ProductRequest> input;
}
