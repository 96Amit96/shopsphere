package com.shopsphere.paymentservice.service;

import com.shopsphere.paymentservice.dto.request.PaymentRequest;
import com.shopsphere.paymentservice.dto.response.PaymentResponse;

public interface PaymentService {

    PaymentResponse createPayment(PaymentRequest request);

    PaymentResponse getPaymentById(Long paymentId);

    PaymentResponse getPaymentByOrderId(Long orderId);

    PaymentResponse processPayment(Long paymentId);
}
