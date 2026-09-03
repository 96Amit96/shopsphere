package com.shopsphere.inventoryservice.controller;

import com.shopsphere.inventoryservice.dto.request.*;
import com.shopsphere.inventoryservice.dto.response.ApiResponse;
import com.shopsphere.inventoryservice.dto.response.InventoryResponse;
import com.shopsphere.inventoryservice.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<ApiResponse<InventoryResponse>> createInventory(
            @Valid @RequestBody InventoryCreateRequest request
            ) {
        InventoryResponse response = inventoryService.createInventory(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Inventory created successfully",
                        response
                ));
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventory(
            @PathVariable Long productId
    ) {

        InventoryResponse response =
                inventoryService.getInventoryByProductId(productId);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Inventory fetched successfully",
                        response
                )
        );
    }

    @PutMapping("/{productId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InventoryResponse>> updateInventory(
            @PathVariable Long productId,
            @Valid @RequestBody InventoryUpdateRequest request
    ) {

        InventoryResponse response =
                inventoryService.updateInventory(
                        productId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Inventory updated successfully",
                        response
                )
        );
    }

    @PatchMapping("/{productId}/stock")
    public ResponseEntity<ApiResponse<InventoryResponse>> adjustStock(
            @PathVariable Long productId,
            @Valid @RequestBody StockAdjustmentRequest request
            ) {

        InventoryResponse response = inventoryService.adjustStock(
                productId,
                request
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Stock adjusted successfully",
                        response
                )
        );
    }

    @PostMapping("/{productId}/reserve")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InventoryResponse>> reserveStock(
            @PathVariable Long productId,
            @Valid @RequestBody StockReservationRequest request
    ) {

        InventoryResponse response =
                inventoryService.reserveStock(
                        productId,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Stock reserved successfully",
                        response
                )
        );
    }

    @PostMapping("/{productId}/release")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InventoryResponse>> releaseStock(
            @PathVariable Long productId,
            @Valid @RequestBody StockReleaseRequest request) {

        InventoryResponse response =
                inventoryService.releaseStock(productId, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Stock released successfully",
                        response
                )
        );
    }

    @PostMapping("/{productId}/deduct")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<InventoryResponse>> deductStock(
            @PathVariable Long productId,
            @Valid @RequestBody StockDeductionRequest request) {

        InventoryResponse response =
                inventoryService.deductStock(productId, request);

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Stock deducted successfully",
                        response
                )
        );
    }
}
