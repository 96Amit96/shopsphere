package com.shopsphere.inventoryservice.service.impl;

import com.shopsphere.inventoryservice.client.ProductClient;
import com.shopsphere.inventoryservice.dto.request.InventoryCreateRequest;
import com.shopsphere.inventoryservice.dto.request.InventoryUpdateRequest;
import com.shopsphere.inventoryservice.dto.request.StockAdjustmentRequest;
import com.shopsphere.inventoryservice.dto.response.InventoryResponse;
import com.shopsphere.inventoryservice.dto.response.ProductResponse;
import com.shopsphere.inventoryservice.entity.Inventory;
import com.shopsphere.inventoryservice.exception.DuplicateResourceException;
import com.shopsphere.inventoryservice.exception.InvalidRequestException;
import com.shopsphere.inventoryservice.exception.ProductNotFoundException;
import com.shopsphere.inventoryservice.exception.ResourceNotFoundException;
import com.shopsphere.inventoryservice.mapper.InventoryMapper;
import com.shopsphere.inventoryservice.repository.InventoryRepository;
import com.shopsphere.inventoryservice.service.InventoryService;
import feign.FeignException;
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
    private final ProductClient productClient;

    @Override
    @Transactional
    public InventoryResponse createInventory(InventoryCreateRequest request) {

        log.info("Creating inventory for productId :: {} , Request :: {}", request.productId() , request);

        if (inventoryRepository.existsByProductId(request.productId())) {
            throw new DuplicateResourceException("Inventory already exists for product id:: "+ request.productId());
        }

        try {
        ProductResponse product = productClient.getProductById(request.productId());
        log.info("product :: {}", product);
        } catch (FeignException.NotFound ex) {
            throw new ProductNotFoundException(
                    "Cannot create inventory. Active product not found with id :: "+
                    request.productId()
            );
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

    @Override
    @Transactional
    public InventoryResponse updateInventory(Long productId, InventoryUpdateRequest request) {

        log.info(
                "Updating inventory. productId: {}, request: {}",
                productId,
                request
        );

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(  ()->
                       new ResourceNotFoundException(
                               "Inventory not found for product id :: "+ productId
                        )
                );

        log.info("Inventory {}", inventory);

        if (request.quantity() < inventory.getReservedQuantity()) {
            throw  new InvalidRequestException("Quantity cannot be less than reserved quantity :: "+ inventory.getReservedQuantity());
        }

        inventory.setQuantity(request.quantity());

        Inventory updatedInventory = inventoryRepository.save(inventory);

        log.info(
                "Inventory updated successfully. productId: {}, quantity: {}",
                productId,
                updatedInventory.getQuantity()
        );


        return inventoryMapper.toResponse(updatedInventory);
    }

    @Override
    @Transactional
    public InventoryResponse adjustStock(Long productId, StockAdjustmentRequest request) {

        log.info( "Adjusting stock. productId: {}, request: {}", productId , request);

        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Inventory not found for product id :: "+
                                        productId
                        )
                        );

        int currentQuantity = inventory.getQuantity();
        int reservedQuantity = inventory.getReservedQuantity();

        int newQuantity = currentQuantity + request.quantityChange();

        if (newQuantity < reservedQuantity) {
            throw new InvalidRequestException("Stock cannot be reduced below reserved quantity :: "+ reservedQuantity);
        }

        inventory.setQuantity(newQuantity);

        Inventory updatedInventory = inventoryRepository.save(inventory);

        log.info(
                "Stock adjusted successfully. productId: {}, oldQuantity: {}, newQuantity: {}",
                productId,
                currentQuantity,
                updatedInventory.getQuantity()
        );

        return inventoryMapper.toResponse(updatedInventory);
    }


}
