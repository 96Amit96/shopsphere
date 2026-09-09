package com.shopsphere.cartservice.client;

import com.shopsphere.cartservice.config.FeignConfig;
import com.shopsphere.cartservice.dto.response.ApiResponse;
import com.shopsphere.cartservice.dto.response.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "product-service",
        configuration = FeignConfig.class
)
public interface ProductClient {

    @GetMapping("/api/v1/products/{id}")
    ApiResponse<ProductResponse> getProductById(@PathVariable("id") Long id);
}
