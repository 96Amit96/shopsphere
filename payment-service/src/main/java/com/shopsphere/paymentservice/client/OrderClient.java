package com.shopsphere.paymentservice.client;

import com.shopsphere.paymentservice.config.FeignConfig;
import com.shopsphere.paymentservice.dto.response.ApiResponse;
import com.shopsphere.paymentservice.dto.response.OrderResponse;
import com.shopsphere.paymentservice.enums.PaymentStatus;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "order-service",
        configuration = FeignConfig.class
)
public interface OrderClient {

    @GetMapping("/api/v1/orders/{orderId}")
    ApiResponse<OrderResponse> getOrderById(
            @PathVariable("orderId") Long orderId
    );

    @PatchMapping("/api/v1/orders/{orderId}/payment-status")
    ApiResponse<OrderResponse> updatePaymentStatus(
            @PathVariable("orderId") Long orderId,
            @RequestParam PaymentStatus paymentStatus
            );

    @PatchMapping("/api/v1/orders/{orderId}/payment-failed")
    ApiResponse<OrderResponse> handlePaymentFailure(
            @PathVariable("orderId") Long orderId
    );
}
