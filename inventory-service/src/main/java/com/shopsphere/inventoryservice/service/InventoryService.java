package com.shopsphere.inventoryservice.service;

import com.shopsphere.inventoryservice.dto.request.InventoryCreateRequest;
import com.shopsphere.inventoryservice.dto.request.InventoryUpdateRequest;
import com.shopsphere.inventoryservice.dto.request.StockAdjustmentRequest;
import com.shopsphere.inventoryservice.dto.response.InventoryResponse;

public interface InventoryService {

    InventoryResponse createInventory(InventoryCreateRequest request);

    InventoryResponse getInventoryByProductId(Long productId);

    InventoryResponse updateInventory(Long productId, InventoryUpdateRequest request);

    InventoryResponse adjustStock(Long productId , StockAdjustmentRequest request);
}
