package com.shopsphere.notificationservice.repository;

import com.shopsphere.notificationservice.entity.UserNotificationProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;

public interface UserNotificationProfileRepository extends JpaRepository<UserNotificationProfile, Long> {

    @Modifying
    @Query(value = """
    INSERT INTO user_notification_profiles (
        user_id,
        first_name,
        last_name,
        email,
        profile_version,
        updated_at
    )
    VALUES (
        :userId,
        :firstName,
        :lastName,
        :email,
        :profileVersion,
        :updatedAt
    )
    ON CONFLICT (user_id)
    DO UPDATE SET
        first_name = EXCLUDED.first_name,
        last_name = EXCLUDED.last_name,
        email = EXCLUDED.email,
        profile_version = EXCLUDED.profile_version,
        updated_at = EXCLUDED.updated_at
    WHERE user_notification_profiles.profile_version
          < EXCLUDED.profile_version
    """, nativeQuery = true)
    int upsertProfile(
            @Param("userId") Long userId,
            @Param("firstName") String firstName,
            @Param("lastName") String lastName,
            @Param("email") String email,
            @Param("profileVersion") Long profileVersion,
            @Param("updatedAt") Instant updatedAt
    );
}
