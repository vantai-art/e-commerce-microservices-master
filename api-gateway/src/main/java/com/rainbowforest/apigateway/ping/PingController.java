package com.rainbowforest.apigateway.ping;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
public class PingController {

    @Value("${ping.user-service-url:https://user-service-7vqq.onrender.com}")
    private String userServiceUrl;

    @Value("${ping.product-catalog-service-url:https://product-catalog-service-qwsl.onrender.com}")
    private String productCatalogUrl;

    @Value("${ping.order-service-url:https://order-service-ahcv.onrender.com}")
    private String orderServiceUrl;

    @Value("${ping.payment-service-url:https://payment-service-8lj4.onrender.com}")
    private String paymentServiceUrl;

    @Value("${ping.product-recommendation-service-url:https://product-recommendation-service.onrender.com}")
    private String recommendationServiceUrl;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @GetMapping("/ping-all")
    public Mono<Map<String, String>> pingAll() {
        Map<String, String> results = new LinkedHashMap<>();
        results.put("user-service", pingUrl(userServiceUrl, "/users"));
        results.put("product-catalog-service", pingUrl(productCatalogUrl, "/products"));
        results.put("order-service", pingUrl(orderServiceUrl, "/order"));
        results.put("payment-service", pingUrl(paymentServiceUrl, "/api/payments"));
        results.put("product-recommendation-service", pingUrl(recommendationServiceUrl, "/recommendations"));
        return Mono.just(results);
    }

    private String pingUrl(String baseUrl, String path) {
        if (baseUrl == null || baseUrl.isBlank())
            return "NOT_CONFIGURED";
        try {
            String url = baseUrl.replaceAll("/+$", "") + path;
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(12))
                    .GET().build();
            int status = http.send(req, HttpResponse.BodyHandlers.discarding()).statusCode();
            return (status < 500) ? "AWAKE (" + status + ")" : "ERROR (HTTP " + status + ")";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}