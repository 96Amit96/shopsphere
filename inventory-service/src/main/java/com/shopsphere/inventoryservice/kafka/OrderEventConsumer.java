package com.shopsphere.inventoryservice.kafka;

import com.shopsphere.inventoryservice.dto.event.InventoryEventType;
import com.shopsphere.inventoryservice.dto.event.InventorySagaEvent;
import com.shopsphere.inventoryservice.dto.event.OrderCreatedEvent;
import com.shopsphere.inventoryservice.exception.InsufficientStockException;
import com.shopsphere.inventoryservice.service.OrderEventProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final OrderEventProcessingService orderEventProcessingService;
    private final InventoryEventProducer inventoryEventProducer;

    @KafkaListener(
            topics = "order-events",
            containerFactory = "orderEventKafkaListenerContainerFactory"
    )
    public void consumeOrderCreatedEvent(OrderCreatedEvent event) {

        log.info(
                "Received OrderCreatedEvent. eventId :: {}, orderId :: {}, userId :: {}, itemCount :: {}",
                event.eventId(),
                event.orderId(),
                event.userId(),
                event.items().size()
        );

     try {

         orderEventProcessingService.processOrderCreatedEvent((event));

     } catch (InsufficientStockException ex) {

         log.error(
                 "Inventory reservation failed. orderId :: {}, reason :: {}",
                 event.orderId(),
                 ex.getMessage()
         );

         InventorySagaEvent failureEvent =
                 new InventorySagaEvent(
                         UUID.randomUUID(),
                         event.eventId(),
                         event.orderId(),
                         event.userId(),
                         InventoryEventType.INVENTORY_RESERVATION_FAILED,
                         List.of(),
                         event.items().get(0).productId(),
                         ex.getMessage(),
                         LocalDateTime.now()
                 );

         inventoryEventProducer.publishInventoryEvent(failureEvent);
     }
    }
}
