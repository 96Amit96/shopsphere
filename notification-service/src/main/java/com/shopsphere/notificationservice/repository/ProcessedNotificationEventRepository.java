package com.shopsphere.notificationservice.repository;

import com.shopsphere.notificationservice.entity.ProcessedNotificationEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface ProcessedNotificationEventRepository extends JpaRepository<ProcessedNotificationEvent, UUID> {

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
}
