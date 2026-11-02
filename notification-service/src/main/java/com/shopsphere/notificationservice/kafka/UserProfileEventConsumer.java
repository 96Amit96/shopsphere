package com.shopsphere.notificationservice.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsphere.notificationservice.dto.event.UserProfileEvent;
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

    @KafkaListener(
            topics = "${notification.user-profile.topic:user-profile-events}",
            groupId = "${spring.kafka.consumer.group-id:notification-service}"
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

        log.info(
                "Processed user profile event. eventId={}, userId={}, version={}",
                event.eventId(),
                event.userId(),
                event.profileVersion()
        );
    }
}
