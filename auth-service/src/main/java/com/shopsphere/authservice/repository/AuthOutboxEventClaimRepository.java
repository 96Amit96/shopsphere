package com.shopsphere.authservice.repository;

import com.shopsphere.authservice.entity.AuthOutboxEvent;

import java.time.LocalDateTime;
import java.util.List;

public interface AuthOutboxEventClaimRepository {

    List<AuthOutboxEvent> claimDuePendingEvents(LocalDateTime now, int batchSize);
}
