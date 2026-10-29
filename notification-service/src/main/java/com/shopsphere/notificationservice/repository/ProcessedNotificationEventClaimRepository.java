package com.shopsphere.notificationservice.repository;

import com.shopsphere.notificationservice.entity.ProcessedNotificationEvent;

import java.time.LocalDateTime;
import java.util.List;

public interface ProcessedNotificationEventClaimRepository {

    List<ProcessedNotificationEvent> claimDuePendingEvents(
            LocalDateTime now,
            int batchSize
    );
}
