package com.shopsphere.orderservice.kafka;

import com.shopsphere.orderservice.dto.event.InventorySagaEvent;
import com.shopsphere.orderservice.dto.event.PaymentRequestedEvent;
import com.shopsphere.orderservice.service.InventoryEventProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

    @Component
    @RequiredArgsConstructor
    @Slf4j
    public class InventoryEventConsumer {

        private final InventoryEventProcessingService processingService;
        private final PaymentRequestedEventProducer paymentRequestedEventProducer;

        @KafkaListener(
                topics = "inventory-events",
                containerFactory =
                        "orderInventoryEventKafkaListenerContainerFactory"
        )
        public void consumeInventoryEvent(InventorySagaEvent event) {

            log.info(
                    "Received InventorySagaEvent. eventId :: {}, orderEventId :: {}, orderId :: {}, eventType :: {}",
                    event.eventId(),
                    event.orderEventId(),
                    event.orderId(),
                    event.eventType()
            );

            PaymentRequestedEvent paymentRequestedEvent = processingService.processInventoryEvent(event);

           if (paymentRequestedEvent != null) {
               paymentRequestedEventProducer.publishPaymentRequested(paymentRequestedEvent);
           }
        }
}
