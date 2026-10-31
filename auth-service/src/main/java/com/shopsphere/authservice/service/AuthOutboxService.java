package com.shopsphere.authservice.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.shopsphere.authservice.dto.event.UserProfileEvent;
import com.shopsphere.authservice.entity.AuthOutboxEvent;
import com.shopsphere.authservice.entity.AuthUser;
import com.shopsphere.authservice.repository.AuthOutboxEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthOutboxService {

    private final AuthOutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void saveUserProfileEvent(AuthUser user, String eventType) {

        UUID eventId = UUID.randomUUID();

        UserProfileEvent userProfileEvent = new UserProfileEvent(
                eventId,
                eventType,
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getProfileVersion(),
                Instant.now()
        );

        try {
            String payload = objectMapper.writeValueAsString(
                    userProfileEvent
            );

            AuthOutboxEvent outboxEvent = new AuthOutboxEvent(
                    eventId,
                    "USER",
                    user.getId(),
                    eventType,
                    payload
            );

            outboxEventRepository.save(outboxEvent);

            log.info(
                    "User profile event stored in outbox. eventId={}, userId={}, eventType={}",
                    eventId,
                    user.getId(),
                    eventType
            );

        } catch (JsonProcessingException exception) {

            throw new IllegalStateException(
                    "Failed to serialize user profile event",
                    exception
            );
        }
    }
}
