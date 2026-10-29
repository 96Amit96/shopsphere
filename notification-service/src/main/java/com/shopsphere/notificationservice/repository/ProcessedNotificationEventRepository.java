package com.shopsphere.notificationservice.repository;

import com.shopsphere.notificationservice.entity.ProcessedNotificationEvent;
import com.shopsphere.notificationservice.enums.NotificationDeliveryStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ProcessedNotificationEventRepository
        extends JpaRepository<ProcessedNotificationEvent, UUID>,
        ProcessedNotificationEventClaimRepository
{

    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO processed_notification_events
                (event_id, event_type, processed_at, payload,
                 status, attempts, next_attempt_at)
            VALUES
                (:eventId, :eventType, CURRENT_TIMESTAMP, :payload,
                 'PENDING', 0, CURRENT_TIMESTAMP)
            ON CONFLICT (event_id) DO NOTHING
            """, nativeQuery = true)
    int enqueueIfAbsent(
            @Param("eventId") UUID eventId,
            @Param("eventType") String eventType,
            @Param("payload") String payload
    );

    // Retained temporarily for the existing consumers.
    @Modifying
    @Transactional
    @Query(value = """
            INSERT INTO processed_notification_events
                (event_id, event_type, processed_at)
            VALUES
                (:eventId, :eventType, CURRENT_TIMESTAMP)
            ON CONFLICT (event_id) DO NOTHING
            """, nativeQuery = true)
    int insertIfAbsent(
            @Param("eventId") UUID eventId,
            @Param("eventType") String eventType
    );

    @Modifying
    @Transactional
    @Query(value = """
        UPDATE processed_notification_events
        SET status = 'PENDING',
            processing_started_at = NULL,
            next_attempt_at = CURRENT_TIMESTAMP,
            last_error = 'Stale processing claim recovered'
        WHERE status = 'PROCESSING'
          AND processing_started_at IS NOT NULL
          AND processing_started_at < :cutoff
        """, nativeQuery = true)
    int recoverStaleProcessingEvents(
            @Param("cutoff") LocalDateTime cutoff
    );

}
