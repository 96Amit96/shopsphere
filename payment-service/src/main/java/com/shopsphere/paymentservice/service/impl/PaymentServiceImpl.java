package com.shopsphere.paymentservice.service.impl;

import com.shopsphere.paymentservice.client.OrderClient;
import com.shopsphere.paymentservice.client.UserClient;
import com.shopsphere.paymentservice.dto.event.PaymentEvent;
import com.shopsphere.paymentservice.dto.request.PaymentRequest;
import com.shopsphere.paymentservice.dto.response.*;
import com.shopsphere.paymentservice.entity.Payment;
import com.shopsphere.paymentservice.enums.PaymentStatus;
import com.shopsphere.paymentservice.exception.DuplicatePaymentException;
import com.shopsphere.paymentservice.exception.PaymentNotFoundException;
import com.shopsphere.paymentservice.exception.PaymentValidationException;
import com.shopsphere.paymentservice.gateway.PaymentGateway;
import com.shopsphere.paymentservice.kafka.PaymentEventProducer;
import com.shopsphere.paymentservice.mapper.PaymentMapper;
import com.shopsphere.paymentservice.repository.PaymentRepository;
import com.shopsphere.paymentservice.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserClient userClient;
    private final PaymentMapper paymentMapper;
    private final OrderClient orderClient;
    private final PaymentGateway paymentGateway;
    private final PaymentEventProducer paymentEventProducer;

    @Override
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {

        log.info("Creating payment for orderId :: {}", request.orderId());

           ApiResponse<OrderResponse> orderResponse =
                   orderClient.getOrderById(request.orderId());

        if (orderResponse == null ||
                orderResponse.data() == null) {

            throw new PaymentNotFoundException(
                    "Order not found with id :: "
                            + request.orderId()
            );
        }

        OrderResponse order = orderResponse.data();
        log.info("Order :: {} ", order);

        // 1. Check duplicate payment
        if (paymentRepository.existsByOrderId(request.orderId())) {
            throw new DuplicatePaymentException("Payment already exists for orderId :: "+ request.orderId());
        }

        // 2. Create Payment entity
        Payment payment = new Payment();

        payment.setOrderId(order.id());
        payment.setUserId(order.userId());
        payment.setAmount(order.totalAmount());
        payment.setPaymentMethod(request.paymentMethod());
        payment.setPaymentStatus(PaymentStatus.PENDING);

        // 3. Generate transaction ID
        payment.setTransactionId(
                "TXN-"+ UUID.randomUUID()
        );

        // 4. Save payment
        Payment savedPayment = paymentRepository.save(payment);

        log.info(
                "Payment created successfully. paymentId :: {}, transactionId :: {}",
                savedPayment.getId(),
                savedPayment.getTransactionId()
        );


        return paymentMapper.toPaymentResponse(savedPayment);
    }

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
    public PaymentResponse processPayment(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() ->
                        new PaymentNotFoundException("Payment not found with id :: "+ paymentId)
                        );

        if (payment.getPaymentStatus() != PaymentStatus.PENDING) {
            throw new PaymentValidationException(
                    "Payment cannot be processed because current status is :: "
                    + payment.getPaymentStatus()
            );
        }

        log.info(
                "Processing payment. paymentId :: {}, orderId :: {}",
                payment.getId(),
                payment.getOrderId()
        );

        PaymentGatewayResponse gatewayResponse =
                paymentGateway.processPayment(payment);

        log.info("Payment response :: {} ", gatewayResponse);

        if (gatewayResponse.successful()) {
            payment.setPaymentStatus(PaymentStatus.SUCCESS);

            log.info(
                    "Payment successful. paymentId :: {}",
                    payment.getId()
            );

        } else {
            payment.setPaymentStatus(PaymentStatus.FAILED);

            log.error(
                    "Payment failed. paymentId :: {}",
                    payment.getId()
            );
        }

        Payment savedPayment = paymentRepository.save(payment);

        PaymentEvent paymentEvent = new PaymentEvent(
                savedPayment.getId(),
                savedPayment.getOrderId(),
                savedPayment.getUserId(),
                savedPayment.getAmount(),
                savedPayment.getPaymentStatus(),
                savedPayment.getTransactionId()

        );

        paymentEventProducer.publishPaymentEvent(paymentEvent);

        return paymentMapper.toPaymentResponse(savedPayment);
    }

}
