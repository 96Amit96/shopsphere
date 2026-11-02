package com.shopsphere.authservice.repository;

import com.shopsphere.authservice.entity.AuthOutboxEvent;
import com.shopsphere.authservice.enums.AuthOutboxStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
public class AuthOutboxEventClaimRepositoryImpl implements AuthOutboxEventClaimRepository{

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<AuthOutboxEvent> claimDuePendingEvents(LocalDateTime now, int batchSize) {

        if (batchSize < 1) {
            throw new IllegalArgumentException(
                    "Batch size must be greater than zero"
            );
        }

        List<?> results = entityManager
                .createNativeQuery("""
                SELECT *
                FROM auth_outbox_events
                WHERE status = 'PENDING'
                  AND next_attempt_at <= :now
                ORDER BY next_attempt_at ASC, created_at ASC
                LIMIT :batchSize
                FOR UPDATE SKIP LOCKED
                """, AuthOutboxEvent.class)
                .setParameter("now", now)
                .setParameter("batchSize", batchSize)
                .getResultList();

        List<AuthOutboxEvent> events = results.stream()
                .map(AuthOutboxEvent.class::cast)
                .toList();

        for (AuthOutboxEvent event : events) {

            event.setStatus(AuthOutboxStatus.PUBLISHING);
            event.setClaimedAt(now);
            event.setClaimToken(UUID.randomUUID());
        }

        entityManager.flush();

        log.debug("Claimed {} auth outbox events", events.size());

        return events;
    }
}
