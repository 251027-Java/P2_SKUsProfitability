package com.example.gateway;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GatewayRouteConfig {

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
                .route("authservice-route", r -> r
                        .path("/auth/**")
                        .filters(f -> f.stripPrefix(1))
                        .uri("lb://AUTH-SERVICE"))
                .route("productservice-route", r -> r
                        .path("/product/**")
                        .filters(f -> f
                                .rewritePath("/product/api/(?<segment>.*)", "/api/${segment}")
                                .rewritePath("/product/(?<segment>.*)", "/api/${segment}"))
                        .uri("lb://PRODUCT-SERVICE"))
                .route("calculatorservice-route", r -> r
                        .path("/calculator/**")
                        .filters(f -> f
                                .rewritePath("/calculator/api/calculator/(?<segment>.*)", "/api/calculator/${segment}")
                                .rewritePath("/calculator/(?<segment>.*)", "/api/calculator/${segment}"))
                        .uri("lb://CALCULATOR-SERVICE"))
                .build();
    }
}

