package com.shopsphere.authservice.kafka;

import com.shopsphere.authservice.entity.AuthOutboxEvent;
import com.shopsphere.authservice.service.AuthOutboxEventService;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthOutboxPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final AuthOutboxEventService outboxEventService;

    @Value("${auth.outbox.topic:user-profile-events}")
    private String topic;

    @Value("${auth.outbox.publisher.batch-size:100}")
    private int batchSize;

    @Value("${auth.outbox.publisher.max-attempts:5}")
    private int maxAttempts;

    @Value("${auth.outbox.publisher.publish-timeout-seconds:10}")
    private long publishTimeoutSeconds;

    @Value("${auth.outbox.publisher.stale-claim-timeout-ms:300000}")
    private long staleClaimTimeoutMs;

    @Scheduled(
            fixedDelayString = "${auth.outbox.publisher.fixed-delay-ms:5000}"
    )
    public void publishPendingEvents() {

        List<AuthOutboxEvent> events =
                outboxEventService.claimDuePendingEvents(batchSize);

        if (events.isEmpty()) {
            return;
        }

        log.info("Claimed {} auth outbox events for publishing", events.size());

        for (AuthOutboxEvent event : events) {

            if (Thread.currentThread().isInterrupted()) {
                break;
            }

            publishEvent(event);
        }
    }

    private void publishEvent(AuthOutboxEvent event) {

        try {
            SendResult<String, String> result = kafkaTemplate
                    .send(
                            topic,
                            String.valueOf(event.getAggregateId()),
                            event.getPayload()
                    )
                    .get(publishTimeoutSeconds, TimeUnit.SECONDS);

            log.info(
                    "Auth outbox event acknowledged by Kafka. eventId={}, userId={}, partition={}, offset={}",
                    event.getEventId(),
                    event.getAggregateId(),
                    result.getRecordMetadata().partition(),
                    result.getRecordMetadata().offset()
            );

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            log.warn(
                    "Interrupted while publishing auth outbox event. eventId={}",
                    event.getEventId()
            );

            return;

        } catch (Exception exception) {

            recordPublicationFailure(event, exception);
            return;
        }

        // Kafka acknowledged the message.
        // Update PostgreSQL in a separate transaction.
        try {
            boolean markedPublished = outboxEventService.markPublished(
                    event.getEventId(),
                    event.getClaimToken()
            );

            if (markedPublished) {
                log.info(
                        "Auth outbox event marked PUBLISHED. eventId={}, userId={}",
                        event.getEventId(),
                        event.getAggregateId()
                );
            } else {
                log.warn(
                        "Ignoring PUBLISHED update because claim is no longer valid. eventId={}",
                        event.getEventId()
                );
            }

        } catch (Exception exception) {

            log.error(
                    "Kafka acknowledged event, but database status update failed. eventId={}. Stale-claim recovery may cause redelivery.",
                    event.getEventId(),
                    exception
            );
        }
    }

    private void recordPublicationFailure(
            AuthOutboxEvent event,
            Exception exception
    ) {

        try {
            boolean recorded = outboxEventService.recordFailure(
                    event,
                    exception,
                    maxAttempts
            );

            if (recorded) {

                int nextAttempt = event.getAttempts() + 1;

                log.error(
                        "Auth outbox publication failed. eventId={}, attempt={}/{}",
                        event.getEventId(),
                        nextAttempt,
                        maxAttempts,
                        exception
                );

                if (nextAttempt >= maxAttempts) {
                    log.error(
                            "Auth outbox event permanently failed. eventId={}",
                            event.getEventId()
                    );
                }

            } else {
                log.warn(
                        "Ignoring failure update because claim is no longer valid. eventId={}",
                        event.getEventId()
                );
            }

        } catch (Exception persistenceException) {

            log.error(
                    "Could not record publication failure. eventId={}",
                    event.getEventId(),
                    persistenceException
            );
        }
    }

    @Scheduled(
            fixedDelayString = "${auth.outbox.publisher.recovery-interval-ms:60000}"
    )
    public void recoverStaleClaims() {

        int recovered = outboxEventService.recoverStaleClaims(
                Duration.ofMillis(staleClaimTimeoutMs),
                maxAttempts
        );

        if (recovered > 0) {
            log.warn(
                    "Recovered {} stale auth outbox claims",
                    recovered
            );
        }
    }

}
