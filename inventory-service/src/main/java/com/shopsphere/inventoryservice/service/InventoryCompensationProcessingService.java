package com.shopsphere.inventoryservice.service;

import com.shopsphere.inventoryservice.dto.event.InventoryCompensationRequestedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class InventoryCompensationProcessingService {

    private final InventoryCompensationTransactionService transactionService;

    public void processCompensation(InventoryCompensationRequestedEvent event) {
        transactionService.releaseReservedStock(event);
    }
}
