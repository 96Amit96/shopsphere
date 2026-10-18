package com.shopsphere.inventoryservice.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "processed_events")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
public class ProcessedEvent {

    @Id
    private UUID eventId;

    private Long orderId;

    private String eventType;

    private LocalDateTime processedAt;

}
