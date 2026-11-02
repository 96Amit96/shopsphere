package com.shopsphere.authservice.repository;

import com.shopsphere.authservice.entity.AuthOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface AuthOutboxEventRepository extends JpaRepository<AuthOutboxEvent, Long>, AuthOutboxEventClaimRepository{
    Optional<AuthOutboxEvent> findByEventId(UUID eventId);

    @Modifying
    @Query(value = """
    UPDATE auth_outbox_events
    SET status = 'PUBLISHED',
        published_at = :publishedAt,
        claimed_at = NULL,
        claim_token = NULL,
        next_attempt_at = NULL,
        last_error = NULL
    WHERE event_id = :eventId
      AND status = 'PUBLISHING'
      AND claim_token = :claimToken
    """, nativeQuery = true)
    int markPublishedIfClaimed(
            @Param("eventId") UUID eventId,
            @Param("claimToken") UUID claimToken,
            @Param("publishedAt") LocalDateTime publishedAt
    );

    @Modifying
    @Query(value = """
    UPDATE auth_outbox_events
    SET attempts = attempts + 1,
        last_error = :lastError,
        claimed_at = NULL,
        claim_token = NULL,
        status = CASE
            WHEN attempts + 1 >= :maxAttempts THEN 'FAILED'
            ELSE 'PENDING'
        END,
        next_attempt_at = CASE
            WHEN attempts + 1 >= :maxAttempts THEN NULL
            ELSE CAST(:nextAttemptAt AS timestamp)
        END
    WHERE event_id = :eventId
      AND status = 'PUBLISHING'
      AND claim_token = :claimToken
    """, nativeQuery = true)
    int recordFailureIfClaimed(
            @Param("eventId") UUID eventId,
            @Param("claimToken") UUID claimToken,
            @Param("lastError") String lastError,
            @Param("maxAttempts") int maxAttempts,
            @Param("nextAttemptAt") LocalDateTime nextAttemptAt
    );

    @Modifying
    @Query(value = """
    UPDATE auth_outbox_events
    SET attempts = attempts + 1,
        status = CASE
            WHEN attempts + 1 >= :maxAttempts THEN 'FAILED'
            ELSE 'PENDING'
        END,
        next_attempt_at = CASE
            WHEN attempts + 1 >= :maxAttempts THEN NULL
            ELSE CURRENT_TIMESTAMP
                 + (LEAST(30 * POWER(2, attempts), 3600)
                    * INTERVAL '1 second')
        END,
        claimed_at = NULL,
        claim_token = NULL,
        last_error = 'Stale publishing claim recovered'
    WHERE status = 'PUBLISHING'
      AND claimed_at IS NOT NULL
      AND claimed_at < :cutoff
    """, nativeQuery = true)
    int recoverStalePublishingEvents(
            @Param("cutoff") LocalDateTime cutoff,
            @Param("maxAttempts") int maxAttempts
    );
}
