package com.rainbowforest.apigateway.ping;

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

    // Gọi qua chính gateway (localhost) để tận dụng lb:// routes đã có sẵn
    private static final String BASE = "http://localhost:8080";

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @GetMapping("/ping-all")
    public Mono<Map<String, String>> pingAll() {
        Map<String, String> results = new LinkedHashMap<>();
        results.put("user-service", ping(BASE + "/users"));
        results.put("product-catalog-service", ping(BASE + "/products"));
        results.put("order-service", ping(BASE + "/order"));
        results.put("payment-service", ping(BASE + "/api/payments"));
        results.put("product-recommendation-service", ping(BASE + "/recommendations"));
        return Mono.just(results);
    }

    private String ping(String url) {
        try {
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(10))
                    .GET().build();
            int status = http.send(req, HttpResponse.BodyHandlers.discarding()).statusCode();
            // 2xx, 4xx đều nghĩa là service đang sống
            return (status < 500) ? "AWAKE (" + status + ")" : "ERROR (HTTP " + status + ")";
        } catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }
}