package com.shopsphere.orderservice.service;

import com.shopsphere.orderservice.client.InventoryClient;
import com.shopsphere.orderservice.dto.request.StockReservationRequest;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryReservationService {

    private final InventoryClient inventoryClient;

    @Retry(name = "inventoryReservationRetry")
    public  void reserveStock(Long productId , StockReservationRequest request) {
        log.info(
                "Attempting inventory reservation. productId :: {}, quantity :: {}",
                productId,
                request.quantity()
        );
        inventoryClient.reserveStock(productId,request);
    }
}
