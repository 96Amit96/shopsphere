package com.shopsphere.inventoryservice.controller;

import com.shopsphere.inventoryservice.dto.request.InventoryCreateRequest;
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
}
