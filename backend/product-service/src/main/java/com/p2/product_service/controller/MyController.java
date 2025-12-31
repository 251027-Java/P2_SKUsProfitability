package com.p2.product_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/hello") // This defines the base path for the class
public class MyController {

    @GetMapping // This handles the GET request to /hello
    public String hello() {
        return "Hello from Eureka Product Service";
    }
}


//
//@RestController
//class MyController {
//    @GetMapping("/hello")
//    public String hello() {
//        return "Hello from Eureka Auth Client";
//    }
//}
