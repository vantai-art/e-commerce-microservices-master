package com.rainbowforest.payment_service.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.Map;

@FeignClient(name = "ORDER-SERVICE")
public interface OrderServiceClient {

    @PutMapping("/api/orders/{orderId}/payment-status")
    Map<String, Object> updatePaymentStatus(
            @PathVariable Long orderId,
            @RequestParam String status
    );
}
