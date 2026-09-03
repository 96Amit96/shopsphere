package com.shopsphere.inventoryservice.service;

import com.shopsphere.inventoryservice.dto.request.*;
import com.shopsphere.inventoryservice.dto.response.InventoryResponse;

public interface InventoryService {

    InventoryResponse createInventory(InventoryCreateRequest request);

    InventoryResponse getInventoryByProductId(Long productId);

    InventoryResponse updateInventory(Long productId, InventoryUpdateRequest request);

    InventoryResponse adjustStock(Long productId , StockAdjustmentRequest request);

    InventoryResponse reserveStock(Long productId, StockReservationRequest request);

    InventoryResponse releaseStock(Long productId, StockReleaseRequest request);

    InventoryResponse deductStock(Long productId, StockDeductionRequest request);
}
