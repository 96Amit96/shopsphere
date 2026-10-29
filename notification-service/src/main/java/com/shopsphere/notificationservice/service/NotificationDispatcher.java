package com.shopsphere.notificationservice.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsphere.notificationservice.dto.event.OrderCreatedEvent;
import com.shopsphere.notificationservice.dto.event.PaymentEvent;
import com.shopsphere.notificationservice.entity.ProcessedNotificationEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationDispatcher {

    private static final int MAX_ATTEMPTS = 5;

    @Value("${notification.dispatcher.stale-claim-timeout-ms:300000}")
    private long staleClaimTimeoutMs;

    private final ObjectMapper objectMapper;
    private final ProcessedNotificationEventService processedEventService;
    private final OrderNotificationService orderNotificationService;
    private final PaymentNotificationService paymentNotificationService;

    @Scheduled(fixedDelayString =
            "${notification.dispatcher.fixed-delay-ms:5000}")
    public void dispatchPendingNotifications() {

        var pendingEvents =
                processedEventService.claimDuePendingEvents();

        for (ProcessedNotificationEvent event : pendingEvents) {
            dispatch(event);
        }
    }

    private void dispatch(ProcessedNotificationEvent event) {

        try {
            switch (event.getEventType()) {

                case "ORDER_CREATED" -> {
                    OrderCreatedEvent orderEvent =
                            objectMapper.readValue(
                                    event.getPayload(),
                                    OrderCreatedEvent.class
                            );

                    orderNotificationService.notifyOrderCreated(orderEvent);
                }

                case "PAYMENT_EVENT" -> {
                    PaymentEvent paymentEvent =
                            objectMapper.readValue(
                                    event.getPayload(),
                                    PaymentEvent.class
                            );

                    paymentNotificationService.notifyPaymentStatus(
                            paymentEvent
                    );
                }

                default -> throw new IllegalArgumentException(
                        "Unsupported notification event type: "
                                + event.getEventType()
                );
            }

            processedEventService.markSent(event.getEventId());

            log.info(
                    "Notification marked SENT. eventId={}, type={}",
                    event.getEventId(),
                    event.getEventType()
            );

        } catch (Exception exception) {

            log.error(
                    "Notification delivery attempt failed. eventId={}, type={}",
                    event.getEventId(),
                    event.getEventType(),
                    exception
            );

            processedEventService.recordFailure(
                    event.getEventId(),
                    exception,
                    MAX_ATTEMPTS
            );
        }
    }

    @Scheduled(fixedDelayString =
            "${notification.dispatcher.recovery-interval-ms:60000}")
    public void recoverStaleNotifications() {

        int recovered = processedEventService.recoverStaleClaims(
                Duration.ofMillis(staleClaimTimeoutMs)
        );

        if (recovered > 0) {
            log.warn(
                    "Recovered {} stale notification claims",
                    recovered
            );
        }
    }

}
