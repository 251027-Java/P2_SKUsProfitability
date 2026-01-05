package com.p2.product_service.mapper;

import com.p2.product_service.model.SKU;
import com.p2.product_service.model.response.BrightDataSkuResponse;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class DataBrightSkuToProductServiceSkuMapper {

    public SKU dataBrightSkuToProductServiceSku(BrightDataSkuResponse response){

        SKU sku = new SKU();
        sku.setSku(response.getAsin());
        sku.setProductName(response.getTitle());
        sku.setSellingPrice(BigDecimal.valueOf(response.getFinalPrice()));
        sku.setDescription(response.getDescription());
        sku.setImageUrl(response.getImageUrl());
        parseWeight(response.getItemWeight(), sku);
        parseDimensions(response.getProductDimensions(), sku);
        sku.setCategory(response.getRootBsCategory());
        return sku;
    }

    private void parseWeight(String itemWeight, SKU sku){
        Pattern pattern = Pattern.compile("\\d+(?:\\.\\d+)?");
        Matcher matcher = pattern.matcher(itemWeight);

        if (matcher.find()) {
            sku.setWeight(BigDecimal.valueOf(Double.parseDouble(matcher.group())));
        }
        else{
            sku.setWeight(BigDecimal.ZERO);
        }

    }


    private void parseDimensions(String productDimensions, SKU sku) {
        Pattern numberPattern = Pattern.compile("\\d+(?:\\.\\d+)?");
        Matcher matcher = numberPattern.matcher(productDimensions);

        List<BigDecimal> values = new ArrayList<>();

        while (matcher.find()) {
            values.add(BigDecimal.valueOf(Double.parseDouble(matcher.group())));
        }

        BigDecimal length = !values.isEmpty() ? values.get(0) : null;
        BigDecimal width  = values.size() > 1 ? values.get(1) : null;
        BigDecimal height = values.size() > 2 ? values.get(2) : null;

        sku.setLength(length);
        sku.setWidth(width);
        sku.setHeight(height);
    }
}
