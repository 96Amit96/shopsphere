package com.shopsphere.authservice.service;

import com.shopsphere.authservice.entity.AuthOutboxEvent;
import com.shopsphere.authservice.repository.AuthOutboxEventRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthOutboxEventService {

    private final AuthOutboxEventRepository repository;

    @Transactional
    public List<AuthOutboxEvent> claimDuePendingEvents(int batchSize) {

        return repository.claimDuePendingEvents(LocalDateTime.now(),batchSize);
    }

    @Transactional
    public boolean markPublished(UUID eventId, UUID claimToken) {

        if (claimToken == null) {
            return false;
        }

        int updatedRows = repository.markPublishedIfClaimed(
                eventId,
                claimToken,
                LocalDateTime.now()
        );

        return updatedRows == 1;
    }

    @Transactional
    public boolean recordFailure(
            AuthOutboxEvent event,
            Exception exception,
            int maxAttempts
    ) {

        if (maxAttempts < 1) {
            throw new IllegalArgumentException(
                    "maxAttempts must be at least 1"
            );
        }

        if (event.getClaimToken() == null) {
            return false;
        }

        int nextAttempt = event.getAttempts() + 1;

        String lastError = exception.getMessage();

        if (lastError == null || lastError.isBlank()) {
            lastError = exception.getClass().getSimpleName();
        }

        // Avoid storing excessively large error messages.
        if (lastError.length() > 4000) {
            lastError = lastError.substring(0, 4000);
        }

        LocalDateTime nextAttemptAt = null;

        if (nextAttempt < maxAttempts) {

            long delaySeconds = Math.min(
                    30L * (1L << Math.min(nextAttempt - 1, 10)),
                    3600L
            );

            nextAttemptAt = LocalDateTime.now()
                    .plusSeconds(delaySeconds);
        }

        int updatedRows = repository.recordFailureIfClaimed(
                event.getEventId(),
                event.getClaimToken(),
                lastError,
                maxAttempts,
                nextAttemptAt
        );

        return updatedRows == 1;
    }

    @Transactional
    public int recoverStaleClaims(
            Duration staleTimeout,
            int maxAttempts
    ) {

        if (staleTimeout == null || staleTimeout.isNegative()
                || staleTimeout.isZero()) {
            throw new IllegalArgumentException(
                    "Stale timeout must be positive"
            );
        }

        if (maxAttempts < 1) {
            throw new IllegalArgumentException(
                    "maxAttempts must be at least 1"
            );
        }

        LocalDateTime cutoff = LocalDateTime.now()
                .minus(staleTimeout);

        return repository.recoverStalePublishingEvents(
                cutoff,
                maxAttempts
        );
    }


}
