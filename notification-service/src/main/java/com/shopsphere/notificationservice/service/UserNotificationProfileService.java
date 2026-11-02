package com.shopsphere.notificationservice.service;

import com.shopsphere.notificationservice.dto.event.UserProfileEvent;
import com.shopsphere.notificationservice.repository.UserNotificationProfileRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserNotificationProfileService {

    private final UserNotificationProfileRepository profileRepository;

    @Transactional
    public void synchronizeProfile(UserProfileEvent event) {

        if (event.userId() == null
                || event.profileVersion() == null
                || event.email() == null
                || event.email().isBlank()
                || event.firstName() == null
                || event.lastName() == null
                || event.occurredAt() == null) {

            throw new IllegalArgumentException(
                    "Invalid user profile event"
            );
        }

        profileRepository.upsertProfile(
                event.userId(),
                event.firstName(),
                event.lastName(),
                event.email(),
                event.profileVersion(),
                event.occurredAt()
        );

        log.info(
                "User notification profile synchronized. userId={}, version={}, eventId={}",
                event.userId(),
                event.profileVersion(),
                event.eventId()
        );
    }
}
