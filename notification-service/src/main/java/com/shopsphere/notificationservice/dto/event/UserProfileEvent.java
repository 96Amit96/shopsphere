package com.shopsphere.notificationservice.dto.event;

import java.time.Instant;
import java.util.UUID;

public record UserProfileEvent(
        UUID eventId,

        String eventType,

        Long userId,

        String firstName,

        String lastName,

        String email,

        Long profileVersion,

        Instant occurredAt
) {
}
