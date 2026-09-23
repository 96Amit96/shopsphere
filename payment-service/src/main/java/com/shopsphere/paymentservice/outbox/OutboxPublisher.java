package com.shopsphere.paymentservice.outbox;

import com.shopsphere.paymentservice.entity.OutboxEvent;
import com.shopsphere.paymentservice.enums.OutboxStatus;
import com.shopsphere.paymentservice.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxPublisher {

    private static final String TOPIC = "payment-events";

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> outboxKafkaTemplate;

    @Scheduled(fixedDelay = 5000)
    public void publishPendingEvents() {

        List<OutboxEvent> events =
                outboxEventRepository
                        .findTop100ByStatusOrderByCreatedAtAsc(
                                OutboxStatus.PENDING
                        );

        for (OutboxEvent event : events) {

            try {

                log.info(
                        "Publishing outbox event. eventId :: {}, aggregateId :: {}",
                        event.getId(),
                        event.getAggregateId()
                );

                outboxKafkaTemplate
                        .send(
                                TOPIC,
                                String.valueOf(event.getAggregateId()),
                                event.getPayload()
                        )
                        .get();

                event.setStatus(
                        OutboxStatus.SENT
                );

                event.setPublishedAt(
                        LocalDateTime.now()
                );

                outboxEventRepository.save(event);

                log.info(
                        "Outbox event published successfully. eventId :: {}",
                        event.getId()
                );

            } catch (Exception ex) {

                log.error(
                        "Failed to publish outbox event. eventId :: {}",
                        event.getId(),
                        ex
                );

                // Keep status PENDING.
                // Scheduler will retry it next time.
            }
        }
    }
}
