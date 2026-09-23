package com.shopsphere.paymentservice.repository;

import com.shopsphere.paymentservice.entity.OutboxEvent;
import com.shopsphere.paymentservice.enums.OutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, UUID> {

    List<OutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
