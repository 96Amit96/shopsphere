package com.shopsphere.inventoryservice.service;

import com.shopsphere.inventoryservice.dto.request.StockReservationRequest;
import com.shopsphere.inventoryservice.dto.response.InventoryResponse;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InventoryReservationRetryService {

    private final InventoryService inventoryService;

    @Retry(name = "inventoryOptimisticLockRetry")
    public InventoryResponse reserveStock(
            Long productId,
            StockReservationRequest request
    ) {

        log.info(
                "Attempting inventory reservation. productId :: {}, quantity :: {}",
                productId,
                request.quantity()
        );

        return inventoryService.reserveStock(productId, request);
    }
}
