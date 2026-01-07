package com.p2.product_service.listener;

import com.p2.product_service.model.SKU;
import com.p2.product_service.service.BasicCalculationService;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

@Component
public class SKUEntityListener implements ApplicationContextAware {

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        SKUEntityListener.applicationContext = applicationContext;
    }

    @PrePersist
    @PreUpdate
    public void calculateSizeClassification(SKU sku) {
        if (sku.getLength() != null && sku.getWidth() != null &&
            sku.getHeight() != null && sku.getWeight() != null && applicationContext != null) {
            BasicCalculationService calculationService = applicationContext.getBean(BasicCalculationService.class);
            String sizeClassification = calculationService.determineSizeTier(
                    sku.getLength(), sku.getWidth(), sku.getHeight(), sku.getWeight());
            sku.setSizeClassification(sizeClassification);
        }
    }
}
