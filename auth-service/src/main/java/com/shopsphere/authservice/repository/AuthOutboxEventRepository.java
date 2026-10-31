package com.shopsphere.authservice.repository;

import com.shopsphere.authservice.entity.AuthOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AuthOutboxEventRepository extends JpaRepository<AuthOutboxEvent, Long> {
    Optional<AuthOutboxEvent> findByEventId(UUID eventId);
}
