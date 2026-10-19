package com.shopsphere.inventoryservice.service;

import com.shopsphere.inventoryservice.dto.event.InventoryDeductionRequestedEvent;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryDeductionProcessingService {

    private final InventoryDeductionTransactionService transactionService;

    @Retry(name = "inventoryOptimisticLockRetry")
    public void processDeduction(InventoryDeductionRequestedEvent event) {
        transactionService.deductReservedStock(event);
    }
}
