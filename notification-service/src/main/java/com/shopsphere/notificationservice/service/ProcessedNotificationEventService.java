package com.shopsphere.notificationservice.service;

import com.shopsphere.notificationservice.entity.ProcessedNotificationEvent;
import com.shopsphere.notificationservice.repository.ProcessedNotificationEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProcessedNotificationEventService {

    private final ProcessedNotificationEventRepository processedNotificationEventRepository;

    public boolean markAsProcessed(UUID eventId, String eventType) {

        if (eventId == null) {
            throw new IllegalArgumentException("Event ID cannot be null");
        }

        int insertedRows =
                processedNotificationEventRepository
                        .insertIfAbsent(eventId, eventType);

        return insertedRows == 1;
    }
}