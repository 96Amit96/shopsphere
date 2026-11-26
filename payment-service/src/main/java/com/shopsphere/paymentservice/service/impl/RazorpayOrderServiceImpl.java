package com.shopsphere.paymentservice.service.impl;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.shopsphere.paymentservice.config.RazorpayProperties;
import com.shopsphere.paymentservice.dto.response.RazorpayOrderResponse;
import com.shopsphere.paymentservice.entity.Payment;
import com.shopsphere.paymentservice.enums.PaymentStatus;
import com.shopsphere.paymentservice.repository.PaymentRepository;
import com.shopsphere.paymentservice.service.RazorpayOrderService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@Slf4j
@RequiredArgsConstructor
public class RazorpayOrderServiceImpl implements RazorpayOrderService {

    private static final String CURRENCY = "INR";

    private final RazorpayClient razorpayClient;
    private final RazorpayProperties razorpayProperties;
    private final PaymentRepository paymentRepository;

    @Override
    public RazorpayOrderResponse createRazorpayOrder(Long paymentId) {

        // 1. Find the existing ShopSphere payment

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(
                        ()-> new EntityNotFoundException(
                                "Payment not found with ID: " + paymentId
                        )
                );

        // 2. Only pending payments can initialize checkout

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new IllegalStateException("Razorpay order can only be created for pending payments");
        }

        // 3. Reuse the existing Razorpay order if already created

        if (payment.getRazorpayOrderId() != null) {
            return buildResponse(payment);
        }

        // 4. Convert INR rupees into paise

        long amountInPaise = convertToPaise(payment.getAmount());

        // 5. Prepare Razorpay order request

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", CURRENCY);
        orderRequest.put("receipt", "shop_payment_" + payment.getId());

        try {
            // 6. Create order using Razorpay SDK

            Order razorpayOrder = razorpayClient.orders.create(orderRequest);
            String razorpayOrderId = razorpayOrder.get("id");

            // 7. Store Razorpay order ID in ShopSphere

            payment.setRazorpayOrderId(razorpayOrderId);
            paymentRepository.save(payment);

            log.info(
                    "Razorpay order created successfully for paymentId: {}",
                    paymentId
            );

            // 8. Return checkout details to frontend
            return buildResponse(payment);

        } catch (RazorpayException ex) {

            log.error(
                    "Failed to create Razorpay order for paymentId: {}",
                    paymentId,
                    ex
            );

            throw new IllegalStateException(
                    "Unable to create Razorpay order. Please try again later.",
                    ex
            );
        }
    }

    private long convertToPaise(BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                    "Payment amount must be greater than zero"
            );
        }

        try {
            return amount.movePointRight(2).longValueExact();
        } catch (ArithmeticException ex) {
            throw new IllegalArgumentException(
                    "Payment amount must have at most two decimal places",
                    ex
            );
        }
    }

    private RazorpayOrderResponse buildResponse(Payment payment) {

        return new RazorpayOrderResponse(
                payment.getId(),
                payment.getOrderId(),
                payment.getRazorpayOrderId(),
                razorpayProperties.keyId(),
                convertToPaise(payment.getAmount()),
                CURRENCY
        );
    }
}
