package com.rainbowforest.payment_service.controller;

import com.rainbowforest.payment_service.dto.PaymentDto.*;
import com.rainbowforest.payment_service.service.payment_service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final payment_service payment_service;

    // POST /api/payments
    // Body: { "orderId":1, "userId":1, "amount":150000, "method":"CASH" }
    @PostMapping
    public ResponseEntity<ApiResponse<PaymentResponse>> create(
            @Valid @RequestBody CreatePaymentRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo thanh toán thành công", payment_service.createPayment(req)));
    }

    // POST /api/payments/{id}/process
    @PostMapping("/{id}/process")
    public ResponseEntity<ApiResponse<PaymentResponse>> process(@PathVariable Long id) {
        return ResponseEntity.ok(
                ApiResponse.success("Xử lý thanh toán hoàn tất", payment_service.processPayment(id)));
    }

    // POST /api/payments/refund
    // Body: { "orderId":1, "reason":"Khách hủy đơn" }
    @PostMapping("/refund")
    public ResponseEntity<ApiResponse<PaymentResponse>> refund(
            @Valid @RequestBody RefundRequest req) {
        return ResponseEntity.ok(
                ApiResponse.success("Hoàn tiền thành công", payment_service.refundPayment(req)));
    }

    // PUT /api/payments/{id}/status
    // Body: { "status":"SUCCESS" }
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<PaymentResponse>> updateStatus(
            @PathVariable Long id,
            @RequestBody UpdateStatusRequest req) {
        return ResponseEntity.ok(
                ApiResponse.success("Cập nhật trạng thái thành công", payment_service.updateStatus(id, req)));
    }

    // GET /api/payments
    @GetMapping
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getAll() {
        return ResponseEntity.ok(ApiResponse.success(payment_service.getAll()));
    }

    // GET /api/payments/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(payment_service.getById(id)));
    }

    // GET /api/payments/order/{orderId}
    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.success(payment_service.getByOrderId(orderId)));
    }

    // GET /api/payments/user/{userId}
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(ApiResponse.success(payment_service.getByUserId(userId)));
    }

    // GET /api/payments/transaction/{transactionId}
    @GetMapping("/transaction/{transactionId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getByTxnId(@PathVariable String transactionId) {
        return ResponseEntity.ok(ApiResponse.success(payment_service.getByTransactionId(transactionId)));
    }
}
