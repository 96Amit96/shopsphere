package com.shopsphere.paymentservice.client;

import com.shopsphere.paymentservice.config.FeignConfig;
import com.shopsphere.paymentservice.dto.response.ApiResponse;
import com.shopsphere.paymentservice.dto.response.OrderResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "order-service",
        configuration = FeignConfig.class
)
public interface OrderClient {

    @GetMapping("/api/v1/orders/{orderId}")
    ApiResponse<OrderResponse> getOrderById(
            @PathVariable("orderId") Long orderId
    );

}
