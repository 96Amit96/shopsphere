package com.shopsphere.inventoryservice.client;

import com.shopsphere.inventoryservice.config.FeignConfig;
import com.shopsphere.inventoryservice.dto.response.ProductResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "product-service",
        configuration = FeignConfig.class
)
public interface ProductClient {

    @GetMapping("/api/v1/products/{id}")
    ProductResponse getProductById(@PathVariable Long id);
}
