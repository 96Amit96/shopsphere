package com.shopsphere.inventoryservice.service.impl;

import com.shopsphere.inventoryservice.dto.request.InventoryCreateRequest;
import com.shopsphere.inventoryservice.dto.response.InventoryResponse;
import com.shopsphere.inventoryservice.entity.Inventory;
import com.shopsphere.inventoryservice.exception.DuplicateResourceException;
import com.shopsphere.inventoryservice.exception.ResourceNotFoundException;
import com.shopsphere.inventoryservice.mapper.InventoryMapper;
import com.shopsphere.inventoryservice.repository.InventoryRepository;
import com.shopsphere.inventoryservice.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final InventoryMapper inventoryMapper;

    @Override
    @Transactional
    public InventoryResponse createInventory(InventoryCreateRequest request) {

        log.info("Creating inventory for productId :: {} , Request :: {}", request.productId() , request);

        if (inventoryRepository.existsByProductId(request.productId())) {
            throw new DuplicateResourceException("Inventory already exists for product id:: "+ request.productId());
        }

        Inventory inventory = inventoryMapper.toEntity(request);
        inventory.setReservedQuantity(0);

        Inventory savedInventory = inventoryRepository.save(inventory);

        log.info("Inventory created successfully Id :: {} , productId :: {}", savedInventory.getId(), savedInventory.getProductId());

        return inventoryMapper.toResponse(savedInventory);
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByProductId(Long productId) {

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Inventory not found for product id: " + productId)
                        );
        return inventoryMapper.toResponse(inventory);
    }
}
