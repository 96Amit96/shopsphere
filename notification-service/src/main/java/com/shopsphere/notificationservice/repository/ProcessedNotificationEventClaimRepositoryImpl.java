package com.shopsphere.notificationservice.repository;

import com.shopsphere.notificationservice.entity.ProcessedNotificationEvent;
import com.shopsphere.notificationservice.enums.NotificationDeliveryStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class ProcessedNotificationEventClaimRepositoryImpl implements ProcessedNotificationEventClaimRepository{

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    public List<ProcessedNotificationEvent> claimDuePendingEvents(
            LocalDateTime now,
            int batchSize) {

        if (batchSize < 1) {
            throw new IllegalArgumentException(
                    "Batch size must be greater than zero");
        }

        List<ProcessedNotificationEvent> events =
                entityManager.createNativeQuery("""
                        SELECT *
                        FROM processed_notification_events
                        WHERE status = 'PENDING'
                          AND (
                              next_attempt_at IS NULL
                              OR next_attempt_at <= :now
                          )
                        ORDER BY next_attempt_at ASC NULLS FIRST,
                                 processed_at ASC
                        LIMIT :batchSize
                        FOR UPDATE SKIP LOCKED
                        """, ProcessedNotificationEvent.class)
                        .setParameter("now", now)
                        .setParameter("batchSize", batchSize)
                        .getResultList();

        for (ProcessedNotificationEvent event : events) {
            event.setStatus(NotificationDeliveryStatus.PROCESSING);
            event.setProcessingStartedAt(now);
            event.setClaimToken(UUID.randomUUID());
            event.setNextAttemptAt(null);
        }

        entityManager.flush();

        return events;
    }
}
