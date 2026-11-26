package com.shopsphere.paymentservice.service.impl;

import com.shopsphere.paymentservice.client.OrderClient;
import com.shopsphere.paymentservice.dto.event.PaymentRequestedEvent;
import com.shopsphere.paymentservice.dto.response.*;
import com.shopsphere.paymentservice.entity.Payment;
import com.shopsphere.paymentservice.enums.PaymentMethod;
import com.shopsphere.paymentservice.enums.PaymentStatus;
import com.shopsphere.paymentservice.exception.PaymentNotFoundException;
import com.shopsphere.paymentservice.exception.PaymentValidationException;
import com.shopsphere.paymentservice.mapper.PaymentMapper;
import com.shopsphere.paymentservice.repository.PaymentRepository;
import com.shopsphere.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentMapper paymentMapper;
    private final OrderClient orderClient;


    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new PaymentNotFoundException(
                                "Payment not found with id :: "+ paymentId
                        ));
        return paymentMapper.toPaymentResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(Long orderId) {

        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() ->
                        new PaymentNotFoundException(
                                "Payment not found for order id :: "+ orderId
                        ));
        return paymentMapper.toPaymentResponse(payment);
    }

    @Override
    @Transactional
    public PaymentResponse createPaymentFromEvent(PaymentRequestedEvent event) {

        log.info(
                "Creating payment from Kafka event. orderId :: {}, userId :: {}",
                event.orderId(),
                event.userId()
        );

        // 1. Validate the event payload
        if (event.eventId() == null
                || event.orderId() == null
                || event.userId() == null
                || event.amount() == null
                || event.amount().signum() <= 0
                || event.paymentMethod() == null
                || event.paymentMethod().isBlank()) {

            throw new PaymentValidationException(
                    "Invalid PaymentRequestedEvent payload"
            );
        }

        // 2. Convert the payment method string to the enum
        PaymentMethod paymentMethod;

        try {
            paymentMethod = PaymentMethod.valueOf(
                    event.paymentMethod().trim().toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException ex) {
            throw new PaymentValidationException(
                    "Unsupported payment method :: " + event.paymentMethod()
            );
        }

        // 3. Make event processing idempotent by order ID
        Payment existingPayment = paymentRepository
                .findByOrderId(event.orderId())
                .orElse(null);

        if (existingPayment != null) {

            log.info(
                    "Payment already exists for orderId :: {}, status :: {}",
                    event.orderId(),
                    existingPayment.getPaymentStatus()
            );

            // A terminal payment must not be charged again on event redelivery.
            return paymentMapper.toPaymentResponse(existingPayment);
        }

        // 4. Create the payment using trusted event data.
        // No protected Order Service API call is needed here.
        Payment payment = new Payment();

        payment.setOrderId(event.orderId());
        payment.setUserId(event.userId());
        payment.setAmount(event.amount());
        payment.setPaymentMethod(paymentMethod);
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setTransactionId("TXN-" + UUID.randomUUID());

        Payment savedPayment = paymentRepository.save(payment);

        log.info(
                "Payment created from event. paymentId :: {}, orderId :: {}",
                savedPayment.getId(),
                savedPayment.getOrderId()
        );

        // 5. Return the newly created PENDING payment.
        // Payment processing will happen through the Razorpay checkout flow.
        return paymentMapper.toPaymentResponse(savedPayment);
    }
}
