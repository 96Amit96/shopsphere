package com.shopsphere.orderservice.client;

import com.shopsphere.orderservice.config.FeignConfig;
import com.shopsphere.orderservice.dto.request.StockReservationRequest;
import com.shopsphere.orderservice.dto.response.ApiResponse;
import com.shopsphere.orderservice.dto.response.InventoryResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "inventory-service",
        configuration = FeignConfig.class
)
public interface InventoryClient {

    @PostMapping("/api/v1/inventory/{productId}/reserve")
    public ResponseEntity<ApiResponse<InventoryResponse>> reserveStock(
            @PathVariable("productId") Long productId,
            @RequestBody StockReservationRequest request
    );

    @PostMapping("/api/v1/inventory/{productId}/release")
    void releaseStock(
            @PathVariable("productId") Long productId,
            @RequestBody StockReservationRequest request
    );
}
