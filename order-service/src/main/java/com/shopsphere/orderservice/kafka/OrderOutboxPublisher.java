package com.shopsphere.orderservice.kafka;

import com.shopsphere.orderservice.entity.OrderOutboxEvent;
import com.shopsphere.orderservice.enums.OrderOutboxStatus;
import com.shopsphere.orderservice.repository.OrderOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderOutboxPublisher {

    private final OrderOutboxEventRepository outboxRepository;

    private final KafkaTemplate<String, String> orderOutboxKafkaTemplate;

    @Scheduled(fixedDelayString = "${shopsphere.outbox.poll-delay-ms:1000}")
    public void publishPendingEvents() {

        List<OrderOutboxEvent> pendingEvents =
                outboxRepository.findTop100ByStatusOrderByCreatedAtAsc(
                        OrderOutboxStatus.PENDING
                );

        for (OrderOutboxEvent event : pendingEvents) {

            try {
                orderOutboxKafkaTemplate.send(
                        event.getTopic(),
                        event.getMessageKey(),
                        event.getPayload()
                ).get(10, TimeUnit.SECONDS);

                event.setStatus(OrderOutboxStatus.SENT);
                event.setPublishedAt(LocalDateTime.now());
                event.setAttempts(event.getAttempts() + 1);
                event.setLastError(null);

                outboxRepository.save(event);

                log.info(
                        "Outbox event published. eventId :: {}, eventType :: {}, " +
                                "topic :: {}, aggregateId :: {}",
                        event.getId(),
                        event.getEventType(),
                        event.getTopic(),
                        event.getAggregateId()
                );

            } catch (Exception ex) {

                event.setAttempts(event.getAttempts() + 1);
                event.setLastError(ex.getMessage());

                outboxRepository.save(event);

                log.error(
                        "Outbox publication failed. eventId :: {}, eventType :: {}, attempts :: {}",
                        event.getId(),
                        event.getEventType(),
                        event.getAttempts(),
                        ex
                );
            }
        }
    }
}
