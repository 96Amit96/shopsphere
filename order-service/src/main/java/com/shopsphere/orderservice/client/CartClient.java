package com.shopsphere.orderservice.client;

import com.shopsphere.orderservice.config.FeignConfig;
import com.shopsphere.orderservice.dto.response.ApiResponse;
import com.shopsphere.orderservice.dto.response.CartResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        name = "cart-service",
        configuration = FeignConfig.class
)
public interface CartClient {

        @GetMapping("/api/v1/cart")
        ApiResponse<CartResponse> getCart();

}
