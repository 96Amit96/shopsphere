package com.shopsphere.paymentservice.controller;

import com.shopsphere.paymentservice.dto.response.ApiResponse;
import com.shopsphere.paymentservice.dto.response.PaymentResponse;
import com.shopsphere.paymentservice.dto.response.RazorpayOrderResponse;
import com.shopsphere.paymentservice.service.PaymentService;
import com.shopsphere.paymentservice.service.RazorpayOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final RazorpayOrderService razorpayOrderService;

    @GetMapping("/{paymentId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(
            @PathVariable Long paymentId) {

        PaymentResponse response =
                paymentService.getPaymentById(paymentId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        new ApiResponse<>(
                                true,
                                "Payment retrieved successfully",
                                response
                        )
                );
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrderId(
            @PathVariable Long orderId) {

        PaymentResponse response =
                paymentService.getPaymentByOrderId(orderId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        new ApiResponse<>(
                                true,
                                "Payment retrieved successfully",
                                response
                        )
                );
    }

    @PostMapping("/{paymentId}/razorpay-order")
    public ResponseEntity<ApiResponse<RazorpayOrderResponse>> createRazorpayOrder(
            @PathVariable Long paymentId) {

        RazorpayOrderResponse response =
                razorpayOrderService.createRazorpayOrder(paymentId);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Razorpay order created successfully",
                        response
                ));
    }
}
