package com.rainbowforest.apigateway.ping;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class PingController {

    @Autowired
    private RestTemplate restTemplate;

    @GetMapping("/ping-all")
    public Map<String, String> pingAll() {
        Map<String, String> results = new LinkedHashMap<>();

        results.put("user-service", ping("http://user-service/users"));
        results.put("product-catalog-service", ping("http://product-catalog-service/products"));
        results.put("order-service", ping("http://order-service/order"));
        results.put("payment-service", ping("http://payment-service/api/payments"));
        results.put("product-recommendation-service", ping("http://product-recommendation-service/recommendations"));

        return results;
    }

    private String ping(String url) {
        try {
            restTemplate.getForObject(url, String.class);
            return "OK";
        } catch (Exception e) {
            // Service trả 401/403/404 vẫn nghĩa là đang chạy → AWAKE
            String msg = e.getMessage();
            if (msg != null && (msg.contains("401") || msg.contains("403") || msg.contains("404"))) {
                return "AWAKE";
            }
            return "ERROR: " + msg;
        }
    }
}