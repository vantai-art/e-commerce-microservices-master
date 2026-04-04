package com.rainbowforest.eurekaserver.ping;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.LinkedHashMap;

@Component
public class PingScheduler {

    // Mỗi 8 phút tự động ping (Render ngủ sau 15 phút)
    private static final long INTERVAL_MS = 8 * 60 * 1000;

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

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

    @Value("${ping.api-gateway-url:https://api-gateway-ng66.onrender.com}")
    private String apiGatewayUrl;

    @Scheduled(fixedRate = INTERVAL_MS, initialDelay = 60000)
    public void pingAll() {
        Map<String, String> targets = new LinkedHashMap<>();
        targets.put("user-service",                   userServiceUrl         + "/users");
        targets.put("product-catalog-service",        productCatalogUrl      + "/products");
        targets.put("order-service",                  orderServiceUrl        + "/order");
        targets.put("payment-service",                paymentServiceUrl      + "/api/payments");
        targets.put("product-recommendation-service", recommendationServiceUrl + "/recommendations");
        targets.put("api-gateway",                    apiGatewayUrl          + "/ping-all");

        System.out.println("[PingScheduler] Bắt đầu ping tất cả services...");
        targets.forEach((name, url) -> {
            String result = ping(url);
            System.out.println("[PingScheduler] " + name + " → " + result);
        });
    }

    private String ping(String url) {
        try {
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
