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

    @Transactional
    public boolean markSent(UUID eventId, UUID claimToken) {
        if (claimToken == null) {
            return false;
        }

        int updatedRows = processedNotificationEventRepository.markSentIfClaimed(
                eventId,
                claimToken,
                LocalDateTime.now()
        );

        return updatedRows == 1;
    }

    @Transactional
    public boolean recordFailure(
            UUID eventId,
            UUID claimToken,
            Exception exception,
            int maxAttempts
    ) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException(
                    "maxAttempts must be at least 1"
            );
        }

        if (claimToken == null) {
            return false;
        }

        ProcessedNotificationEvent event = getEvent(eventId);

        // The next failure attempt number.
        int nextAttempt = event.getAttempts() + 1;

        String lastError = exception.getMessage();
        if (lastError == null || lastError.isBlank()) {
            lastError = exception.getClass().getSimpleName();
        }

        LocalDateTime nextAttemptAt = null;

        if (nextAttempt < maxAttempts) {
            // Exponential backoff: 30, 60, 120, 240... seconds.
            long delaySeconds = Math.min(
                    30L * (1L << Math.min(nextAttempt - 1, 10)),
                    3600L
            );

            nextAttemptAt = LocalDateTime.now()
                    .plusSeconds(delaySeconds);
        }

        int updatedRows = processedNotificationEventRepository.recordFailureIfClaimed(
                eventId,
                claimToken,
                lastError,
                maxAttempts,
                nextAttemptAt
        );

        return updatedRows == 1;
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