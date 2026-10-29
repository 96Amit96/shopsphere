package com.shopsphere.notificationservice.service;

import com.shopsphere.notificationservice.entity.ProcessedNotificationEvent;
import com.shopsphere.notificationservice.enums.NotificationDeliveryStatus;
import com.shopsphere.notificationservice.repository.ProcessedNotificationEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProcessedNotificationEventService {

    private final ProcessedNotificationEventRepository processedNotificationEventRepository;

    public boolean enqueueIfAbsent(
            UUID eventId,
            String eventType,
            String payload) {

        if (eventId == null) {
            throw new IllegalArgumentException(
                    "Event ID cannot be null");
        }

        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException(
                    "Event type cannot be blank");
        }

        if (payload == null || payload.isBlank()) {
            throw new IllegalArgumentException(
                    "Event payload cannot be blank");
        }

        return processedNotificationEventRepository.enqueueIfAbsent(
                eventId,
                eventType,
                payload
        ) == 1;
    }

    @Transactional
    public List<ProcessedNotificationEvent> claimDuePendingEvents() {

        return processedNotificationEventRepository.claimDuePendingEvents(
                LocalDateTime.now(),
                100
        );
    }

    /**
     * Mark a notification as successfully delivered.
     */
    @Transactional
    public void markSent(UUID eventId) {

        ProcessedNotificationEvent event = getEvent(eventId);

        event.setStatus(NotificationDeliveryStatus.SENT);
        event.setSentAt(LocalDateTime.now());
        event.setNextAttemptAt(null);
        event.setLastError(null);
        event.setProcessingStartedAt(null);

        processedNotificationEventRepository.save(event);
    }

    /**
     * Record a failed attempt and schedule a retry,
     * or mark the notification as permanently failed.
     */
    @Transactional
    public void recordFailure(
            UUID eventId,
            Exception exception,
            int maxAttempts) {

        if (maxAttempts < 1) {
            throw new IllegalArgumentException(
                    "Maximum attempts must be at least 1");
        }

        ProcessedNotificationEvent event = getEvent(eventId);

        int attempts = event.getAttempts() + 1;
        event.setAttempts(attempts);

        String errorMessage = exception.getMessage();

        if (errorMessage == null || errorMessage.isBlank()) {
            errorMessage = exception.getClass().getSimpleName();
        }

        event.setLastError(errorMessage);
        event.setProcessingStartedAt(null);

        if (attempts >= maxAttempts) {
            event.setStatus(NotificationDeliveryStatus.FAILED);
            event.setNextAttemptAt(null);

        } else {
            event.setStatus(NotificationDeliveryStatus.PENDING);

            // Exponential backoff: 30s, 60s, 120s, up to 1 hour.
            long delaySeconds = Math.min(
                    30L * (1L << Math.min(attempts - 1, 7)),
                    3600L
            );

            event.setNextAttemptAt(
                    LocalDateTime.now().plusSeconds(delaySeconds)
            );
        }

        processedNotificationEventRepository.save(event);
    }

    private ProcessedNotificationEvent getEvent(UUID eventId) {
        return processedNotificationEventRepository.findById(eventId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Notification event not found: " + eventId));
    }

    /**
     * Legacy method retained until both consumers use enqueueIfAbsent().
     */
    @Deprecated
    public boolean markAsProcessed(UUID eventId, String eventType) {

        if (eventId == null) {
            throw new IllegalArgumentException(
                    "Event ID cannot be null");
        }

        return processedNotificationEventRepository.insertIfAbsent(eventId, eventType) == 1;
    }

    @Transactional
    public int recoverStaleClaims(Duration staleTimeout) {

        if (staleTimeout == null
                || staleTimeout.isZero()
                || staleTimeout.isNegative()) {
            throw new IllegalArgumentException(
                    "Stale timeout must be positive");
        }

        LocalDateTime cutoff = LocalDateTime.now().minus(staleTimeout);

        return processedNotificationEventRepository.recoverStaleProcessingEvents(cutoff);
    }
}