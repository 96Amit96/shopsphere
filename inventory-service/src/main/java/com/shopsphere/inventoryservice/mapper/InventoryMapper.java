package com.shopsphere.inventoryservice.mapper;

import com.shopsphere.inventoryservice.dto.request.InventoryCreateRequest;
import com.shopsphere.inventoryservice.dto.response.InventoryResponse;
import com.shopsphere.inventoryservice.entity.Inventory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface InventoryMapper {

    Inventory toEntity(InventoryCreateRequest request);

    @Mapping(
            target = "availableQuantity",
            expression = "java(inventory.getAvailableQuantity())"
    )
    InventoryResponse toResponse(Inventory inventory);
}
