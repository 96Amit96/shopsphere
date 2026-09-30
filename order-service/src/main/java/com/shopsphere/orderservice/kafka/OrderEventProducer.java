package com.shopsphere.orderservice.kafka;

import com.shopsphere.orderservice.dto.event.OrderCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventProducer {

    private static final String ORDER_EVENTS_TOPIC = "order-events";

    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public void publishOrderCreated(OrderCreatedEvent event) {

        kafkaTemplate.send(
                ORDER_EVENTS_TOPIC,
                event.orderId().toString(),
                event
        );

        log.info(
                "Published OrderCreatedEvent. eventId :: {}, orderId :: {}",
                event.eventId(),
                event.orderId()
        );
    }


}
