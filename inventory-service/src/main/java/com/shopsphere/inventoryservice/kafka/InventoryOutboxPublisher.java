package com.shopsphere.inventoryservice.kafka;

import com.shopsphere.inventoryservice.entity.InventoryOutboxEvent;
import com.shopsphere.inventoryservice.enums.InventoryOutboxStatus;
import com.shopsphere.inventoryservice.repository.InventoryOutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class InventoryOutboxPublisher {

    private final InventoryOutboxEventRepository inventoryOutboxEventRepository;
    private final KafkaTemplate<String, String> inventoryOutboxKafkaTemplate;

    public InventoryOutboxPublisher(
            InventoryOutboxEventRepository inventoryOutboxEventRepository,
            @Qualifier("inventoryOutboxKafkaTemplate")
            KafkaTemplate<String, String> inventoryOutboxKafkaTemplate) {

        this.inventoryOutboxEventRepository = inventoryOutboxEventRepository;
        this.inventoryOutboxKafkaTemplate = inventoryOutboxKafkaTemplate;
    }

    @Scheduled(fixedDelayString = "${shopsphere.outbox.poll-delay-ms:1000}")
    public void publishPendingEvents() {

        List<InventoryOutboxEvent> pendingEvents =
                inventoryOutboxEventRepository
                        .findTop100ByStatusOrderByCreatedAtAsc(
                                InventoryOutboxStatus.PENDING
                        );

        for (InventoryOutboxEvent event : pendingEvents) {
            publishEvent(event);
        }
    }

    private void publishEvent(InventoryOutboxEvent event) {

        try {
            log.info(
                    "Publishing inventory outbox event. eventId :: {}, "
                            + "eventType :: {}, topic :: {}, aggregateId :: {}",
                    event.getId(),
                    event.getEventType(),
                    event.getTopic(),
                    event.getAggregateId()
            );

            inventoryOutboxKafkaTemplate.send(
                    event.getTopic(),
                    event.getMessageKey(),
                    event.getPayload()
            ).get(10, TimeUnit.SECONDS);

            event.setStatus(InventoryOutboxStatus.SENT);
            event.setPublishedAt(LocalDateTime.now());
            event.setAttempts(event.getAttempts() + 1);
            event.setLastError(null);

            inventoryOutboxEventRepository.save(event);

            log.info(
                    "Inventory outbox event published successfully. "
                            + "eventId :: {}, eventType :: {}, topic :: {}",
                    event.getId(),
                    event.getEventType(),
                    event.getTopic()
            );

        } catch (Exception exception) {

            event.setAttempts(event.getAttempts() + 1);
            event.setLastError(exception.getMessage());

            inventoryOutboxEventRepository.save(event);

            log.error(
                    "Failed to publish inventory outbox event. "
                            + "eventId :: {}, topic :: {}",
                    event.getId(),
                    event.getTopic(),
                    exception
            );
        }
    }
}
