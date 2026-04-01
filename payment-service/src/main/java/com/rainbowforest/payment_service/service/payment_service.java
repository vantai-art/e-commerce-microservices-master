package com.rainbowforest.payment_service.service;

import com.rainbowforest.payment_service.client.OrderServiceClient;
import com.rainbowforest.payment_service.dto.PaymentDto.*;
import com.rainbowforest.payment_service.model.Payment;
import com.rainbowforest.payment_service.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class payment_service {

    private final PaymentRepository paymentRepository;
    private final OrderServiceClient orderServiceClient;

    // ===================== CREATE =====================

    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest req) {
        log.info("Tạo thanh toán: orderId={}, amount={}", req.getOrderId(), req.getAmount());

        // Kiểm tra đơn hàng đã thanh toán thành công chưa
        boolean alreadyPaid = paymentRepository.findByOrderId(req.getOrderId())
                .stream().anyMatch(p -> p.getStatus() == Payment.PaymentStatus.SUCCESS);
        if (alreadyPaid) {
            throw new RuntimeException("Đơn hàng #" + req.getOrderId() + " đã được thanh toán thành công");
        }

        Payment payment = Payment.builder()
                .orderId(req.getOrderId())
                .userId(req.getUserId())
                .amount(req.getAmount())
                .method(req.getMethod())
                .status(Payment.PaymentStatus.PENDING)
                .build();

        return PaymentResponse.from(paymentRepository.save(payment));
    }

    // ===================== PROCESS =====================

    @Transactional
    public PaymentResponse processPayment(Long paymentId) {
        Payment payment = findById(paymentId);

        if (payment.getStatus() != Payment.PaymentStatus.PENDING) {
            throw new RuntimeException("Chỉ xử lý được thanh toán ở trạng thái PENDING");
        }

        boolean success = simulateGateway(payment);

        if (success) {
            payment.setStatus(Payment.PaymentStatus.SUCCESS);
            payment.setFailureReason(null);
            notifyOrder(payment.getOrderId(), "PAID");
            log.info("Thanh toán SUCCESS: id={}", paymentId);
        } else {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setFailureReason("Giao dịch bị từ chối bởi ngân hàng");
            notifyOrder(payment.getOrderId(), "PAYMENT_FAILED");
            log.warn("Thanh toán FAILED: id={}", paymentId);
        }

        return PaymentResponse.from(paymentRepository.save(payment));
    }

    // ===================== REFUND =====================

    @Transactional
    public PaymentResponse refundPayment(RefundRequest req) {
        Payment payment = paymentRepository
                .findTopByOrderIdOrderByCreatedAtDesc(req.getOrderId())
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy thanh toán cho đơn hàng #" + req.getOrderId()));

        if (payment.getStatus() != Payment.PaymentStatus.SUCCESS) {
            throw new RuntimeException("Chỉ hoàn tiền được thanh toán đã SUCCESS");
        }

        payment.setStatus(Payment.PaymentStatus.REFUNDED);
        payment.setFailureReason("Hoàn tiền: " + (req.getReason() != null ? req.getReason() : "Không rõ lý do"));
        notifyOrder(payment.getOrderId(), "REFUNDED");

        return PaymentResponse.from(paymentRepository.save(payment));
    }

    // ===================== UPDATE STATUS (admin/test) =====================

    @Transactional
    public PaymentResponse updateStatus(Long paymentId, UpdateStatusRequest req) {
        Payment payment = findById(paymentId);
        payment.setStatus(req.getStatus());
        if (req.getFailureReason() != null) payment.setFailureReason(req.getFailureReason());
        return PaymentResponse.from(paymentRepository.save(payment));
    }

    // ===================== QUERY =====================

    public PaymentResponse getById(Long id) {
        return PaymentResponse.from(findById(id));
    }

    public PaymentResponse getByTransactionId(String txnId) {
        return paymentRepository.findByTransactionId(txnId)
                .map(PaymentResponse::from)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch: " + txnId));
    }

    public List<PaymentResponse> getByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .stream().map(PaymentResponse::from).collect(Collectors.toList());
    }

    public List<PaymentResponse> getByUserId(Long userId) {
        return paymentRepository.findByUserId(userId)
                .stream().map(PaymentResponse::from).collect(Collectors.toList());
    }

    public List<PaymentResponse> getAll() {
        return paymentRepository.findAll()
                .stream().map(PaymentResponse::from).collect(Collectors.toList());
    }

    // ===================== PRIVATE =====================

    private Payment findById(Long id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy payment id: " + id));
    }

    /**
     * Giả lập cổng thanh toán:
     * CASH       → luôn SUCCESS
     * Các loại khác → 80% SUCCESS
     */
    private boolean simulateGateway(Payment payment) {
        if (payment.getMethod() == Payment.PaymentMethod.CASH) return true;
        return Math.random() < 0.8;
    }

    private void notifyOrder(Long orderId, String status) {
        try {
            orderServiceClient.updatePaymentStatus(orderId, status);
        } catch (Exception e) {
            log.warn("Không thể cập nhật Order Service (orderId={}): {}", orderId, e.getMessage());
        }
    }
}
