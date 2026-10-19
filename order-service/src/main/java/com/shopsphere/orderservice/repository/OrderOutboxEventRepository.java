package com.shopsphere.orderservice.repository;

import com.shopsphere.orderservice.entity.OrderOutboxEvent;
import com.shopsphere.orderservice.enums.OrderOutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderOutboxEventRepository extends JpaRepository<OrderOutboxEvent, UUID> {

    List<OrderOutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(
            OrderOutboxStatus status
    );
}
