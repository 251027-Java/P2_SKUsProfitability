package com.p2.product_service.model.request.BrightData;

import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;

@Data
@NoArgsConstructor
public class BrightDataCollectByUrlRequest {
     List<BrightDataProductDetailsRequest> input;
}
