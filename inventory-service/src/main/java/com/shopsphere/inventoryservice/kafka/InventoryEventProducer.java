package com.shopsphere.inventoryservice.kafka;

import com.shopsphere.inventoryservice.dto.event.InventorySagaEvent;
import com.shopsphere.inventoryservice.service.InventoryOutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class InventoryEventProducer {

   private final InventoryOutboxService inventoryOutboxService;

   public void publishInventoryEvent(InventorySagaEvent event) {

       inventoryOutboxService.saveInventorySagaEvent(event);

       log.info(
               "InventorySagaEvent recorded in outbox. eventId :: {}, "
                       + "orderEventId :: {}, orderId :: {}, eventType :: {}",
               event.eventId(),
               event.orderEventId(),
               event.orderId(),
               event.eventType()
       );
   }
}
