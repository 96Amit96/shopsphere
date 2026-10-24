package com.shopsphere.inventoryservice.repository;

import com.shopsphere.inventoryservice.entity.InventoryOutboxEvent;
import com.shopsphere.inventoryservice.enums.InventoryOutboxStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InventoryOutboxEventRepository extends JpaRepository<InventoryOutboxEvent, UUID> {

    List<InventoryOutboxEvent> findTop100ByStatusOrderByCreatedAtAsc(InventoryOutboxStatus status);
}
