package com.shopsphere.inventoryservice.service;

import com.shopsphere.inventoryservice.dto.request.InventoryCreateRequest;
import com.shopsphere.inventoryservice.dto.response.InventoryResponse;

public interface InventoryService {

    InventoryResponse createInventory(InventoryCreateRequest request);

    InventoryResponse getInventoryByProductId(Long productId);
}
