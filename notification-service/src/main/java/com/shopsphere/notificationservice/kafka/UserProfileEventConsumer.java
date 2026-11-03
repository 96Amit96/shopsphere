package com.shopsphere.notificationservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsphere.notificationservice.dto.event.UserProfileEvent;
import com.shopsphere.notificationservice.service.ProcessedNotificationEventService;
import com.shopsphere.notificationservice.service.UserNotificationProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserProfileEventConsumer {

    private final ObjectMapper objectMapper;
    private final UserNotificationProfileService profileService;
    private final ProcessedNotificationEventService processedNotificationEventService;

    @KafkaListener(
            topics = "${notification.user-profile.topic:user-profile-events}",
            groupId = "${notification.user-profile.group-id:notification-user-profile}"
    )
    public void consumeUserProfileEvent(String message)
            throws JsonProcessingException {

        log.info("Received user profile event: {}", message);

        UserProfileEvent event =
                objectMapper.readValue(message, UserProfileEvent.class);

        if (!"USER_PROFILE_CREATED".equals(event.eventType())
                && !"USER_PROFILE_UPDATED".equals(event.eventType())) {
            throw new IllegalArgumentException(
                    "Unsupported user profile event type: " + event.eventType()
            );
        }

        profileService.synchronizeProfile(event);

        if ("USER_PROFILE_CREATED".equals(event.eventType())) {
            processedNotificationEventService.enqueueIfAbsent(
                    event.eventId(),
                    event.eventType(),
                    message
            );

            log.info(
                    "Welcome notification enqueued. userId={}, eventId={}",
                    event.userId(),
                    event.eventId()
            );
        }
    }
}
