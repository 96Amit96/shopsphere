package com.shopsphere.notificationservice.entity;

import com.shopsphere.notificationservice.enums.NotificationDeliveryStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "processed_notification_events")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedNotificationEvent {

    @Id
    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @Column(name = "processed_at", nullable = false, updatable = false)
    private LocalDateTime processedAt;

    @Column(name = "payload", columnDefinition = "TEXT")
    private String payload;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private NotificationDeliveryStatus status =
            NotificationDeliveryStatus.PENDING;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastError;

    @Column(name = "processing_started_at")
    private LocalDateTime processingStartedAt;

    @Column(name = "next_attempt_at")
    private LocalDateTime nextAttemptAt;

    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    public ProcessedNotificationEvent(
            UUID eventId,
            String eventType,
            String payload) {

        this.eventId = eventId;
        this.eventType = eventType;
        this.payload = payload;
        this.status = NotificationDeliveryStatus.PENDING;
        this.attempts = 0;
    }

    @PrePersist
    protected void onCreate() {
        if (processedAt == null) {
            processedAt = LocalDateTime.now();
        }

        if (status == null) {
            status = NotificationDeliveryStatus.PENDING;
        }
    }
}
