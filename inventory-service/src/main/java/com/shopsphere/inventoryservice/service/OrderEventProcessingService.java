package com.shopsphere.inventoryservice.service;

import com.shopsphere.inventoryservice.dto.event.OrderCreatedEvent;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventProcessingService {

    private final OrderEventTransactionService transactionService;

    @Retry(name = "inventoryOptimisticLockRetry")
    public boolean processOrderCreatedEvent(OrderCreatedEvent event) {

        log.info(
                "Processing OrderCreatedEvent through Saga. eventId :: {}, orderId :: {}",
                event.eventId(),
                event.orderId()
        );

        return transactionService.processOrderCreatedEvent(event);

    }
}
