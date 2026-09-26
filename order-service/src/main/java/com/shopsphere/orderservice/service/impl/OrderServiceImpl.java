package com.shopsphere.orderservice.service.impl;

import com.shopsphere.orderservice.client.CartClient;
import com.shopsphere.orderservice.client.InventoryClient;
import com.shopsphere.orderservice.client.UserClient;
import com.shopsphere.orderservice.dto.event.PaymentEvent;
import com.shopsphere.orderservice.dto.request.CreateOrderRequest;
import com.shopsphere.orderservice.dto.request.StockReservationRequest;
import com.shopsphere.orderservice.dto.response.*;
import com.shopsphere.orderservice.entity.Order;
import com.shopsphere.orderservice.entity.OrderItem;
import com.shopsphere.orderservice.entity.ProcessedEvent;
import com.shopsphere.orderservice.enums.OrderStatus;
import com.shopsphere.orderservice.enums.PaymentStatus;
import com.shopsphere.orderservice.exception.InventoryReservationException;
import com.shopsphere.orderservice.exception.InventoryServiceUnavailableException;
import com.shopsphere.orderservice.exception.ResourceNotFoundException;
import com.shopsphere.orderservice.repository.OrderRepository;
import com.shopsphere.orderservice.repository.ProcessedEventRepository;
import com.shopsphere.orderservice.service.InventoryReservationService;
import com.shopsphere.orderservice.service.OrderService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserClient userClient;
    private final CartClient cartClient;
    private final InventoryClient inventoryClient;
    private final ProcessedEventRepository processedEventRepository;
    private final InventoryReservationService inventoryReservationService;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {

        log.info("Create order request :: {}", request );

        // 1. Get authenticated user
        ApiResponse<CurrentUserResponse> currentUserResponse
                = userClient.getCurrentUser();

        Long userId = currentUserResponse.data().id();

        // 2. Get user's cart
        ApiResponse<CartResponse> cartResponse =cartClient.getCart();
        log.info("Cart :: {}", cartResponse.data().id());
        CartResponse cart = cartResponse.data();

        // 3. Validate cart
        if (cart.items() == null || cart.items().isEmpty()) {
            throw new IllegalStateException("Cannot create order from an empty cart");
        }

        // 4. Create Order
        Order order = new Order();
        order.setUserId(userId);

        // 5. Convert CartItems → OrderItems
        for (CartItemResponse cartItem : cart.items()) {
            OrderItem orderItem = new OrderItem();

            orderItem.setProductId(cartItem.productId());
            orderItem.setProductName(cartItem.productName());
            orderItem.setUnitPrice(cartItem.unitPrice());
            orderItem.setQuantity(cartItem.quantity());

            order.addItem(orderItem);
        }

        // 6. Calculate total
        order.setTotalAmount(
                order.getItems()
                        .stream()
                        .map(item ->
                                item.getUnitPrice()
                                        .multiply(
                                                java.math.BigDecimal.valueOf(
                                                        item.getQuantity()
                                                )
                                        )
                                )
                        .reduce(
                                java.math.BigDecimal.ZERO,
                                java.math.BigDecimal::add
                        )
        );

        // 7. Save Order
        Order savedOrder = orderRepository.save(order);
        log.info("Saved order :: {}", savedOrder.getId());

        //8. Reserve stock

        List<OrderItem> reservedItems = new ArrayList<>();

        try {
            for (OrderItem item : savedOrder.getItems()) {
                StockReservationRequest reservationRequest =
                        new StockReservationRequest(item.getQuantity());

                inventoryReservationService.reserveStock(
                        item.getProductId(),
                        reservationRequest
                );

                reservedItems.add(item);
            }
        } catch (InventoryReservationException ex) {

            log.info(
                    "Inventory reservation failed for orderId :: {}",
                    savedOrder.getId()
            );

            releaseReservedStock(reservedItems);

            savedOrder.setOrderStatus((OrderStatus.CANCELLED));
            orderRepository.save(savedOrder);

            throw new InventoryReservationException( "Unable to reserve inventory for order");
        } catch (InventoryServiceUnavailableException ex) {
            // Feign + Resilience4j fallback
            log.error(
                    "Inventory service unavailable for orderId :: {}",
                    savedOrder.getId(),
                    ex.getMessage()
            );

            // Release any stock that may already have been reserved
            releaseReservedStock(reservedItems);

            savedOrder.setOrderStatus(OrderStatus.CANCELLED);
            orderRepository.save(savedOrder);

            throw new InventoryServiceUnavailableException(
                    "Inventory service is currently unavailable. "
                            + "Order cannot be created at the moment."
            );
        }

        // 8. Map response
        return mapToOrderResponse(savedOrder);
    }


    //Release reservedStock compensation method

    private void releaseReservedStock(
            List<OrderItem> reservedItems
    ) {

        for (OrderItem item : reservedItems) {

            try {

                StockReservationRequest request =
                        new StockReservationRequest(
                                item.getQuantity()
                        );

                inventoryClient.releaseStock(
                        item.getProductId(),
                        request
                );

                log.info(
                        "Released inventory for productId :: {}, quantity :: {}",
                        item.getProductId(),
                        item.getQuantity()
                );

            } catch (FeignException ex) {

                log.info(
                        "Failed to release inventory for productId :: {}",
                        item.getProductId(),
                        ex
                );
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders() {

        ApiResponse<CurrentUserResponse> currentUserResponse =  userClient.getCurrentUser();

        CurrentUserResponse currentUser = currentUserResponse.data();
        log.info("Logged in user :: {}", currentUser);

        Long userId = currentUser.id();

        return orderRepository.findByUserId(userId)
                .stream()
                .map(this::mapToOrderResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getMyOrder(Long orderId) {

        ApiResponse<CurrentUserResponse> currentUserResponse =  userClient.getCurrentUser();

        CurrentUserResponse currentUser = currentUserResponse.data();
        log.info("Logged in user :: {}", currentUser);

        Long userId = currentUser.id();

        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() ->
                       new ResourceNotFoundException(
                               "Order not found with id :: " + orderId
                       ));

        return mapToOrderResponse(order);
    }

    @Override
    @Transactional
    public OrderResponse updatePaymentStatus(Long orderId, PaymentStatus paymentStatus) {

        log.info(
                "Updating payment status for orderId :: {}, paymentStatus :: {}",
                orderId,
                paymentStatus
        );
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found with id :: " + orderId
                        )
                );

        if (paymentStatus == PaymentStatus.SUCCESS) {

            if (order.getOrderStatus() == OrderStatus.CANCELLED) {
                throw new IllegalStateException("Cannot confirm a cancelled order :: " + orderId);
            }
            order.setPaymentStatus(PaymentStatus.SUCCESS);
            order.setOrderStatus(OrderStatus.CONFIRMED);
        }

        Order updatedOrder =
                orderRepository.save(order);
        log.info(
                "Payment status updated successfully for orderId :: {}",
                orderId
        );

        return mapToOrderResponse(updatedOrder);
    }

    @Override
    @Transactional
    public OrderResponse handlePaymentFailure(Long orderId) {

        log.info(
                "Handling payment failure for orderId :: {}",
                orderId
        );

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found with id :: " + orderId
                        )
                );

        // Prevent duplicate compensation
        if (order.getPaymentStatus() == PaymentStatus.FAILED
                && order.getOrderStatus() == OrderStatus.CANCELLED) {

            log.info(
                    "Payment failure already handled for orderId :: {}",
                    orderId
            );

            return mapToOrderResponse(order);
        }

        // Release reserved inventory
        releaseReservedStock(order.getItems());

        // Update statuses
        order.setPaymentStatus(PaymentStatus.FAILED);
        order.setOrderStatus(OrderStatus.CANCELLED);

        Order updatedOrder =
                orderRepository.save(order);

        log.info(
                "Payment failure handled successfully. orderId :: {}",
                orderId
        );

        return mapToOrderResponse(updatedOrder);
    }

    @Override
    @Transactional
    public void processPaymentEvent(PaymentEvent event) {

        log.info(
                "Processing payment event. eventId :: {}, orderId :: {}, status :: {}",
                event.eventId(),
                event.orderId(),
                event.paymentStatus()
        );

        // 1. Idempotency check
        if (processedEventRepository.existsById(event.eventId())) {
            log.info(
                    "Payment event already processed. eventId :: {}",
                    event.eventId()
            );

            return;
        }

        // 2. Process business event
        switch (event.paymentStatus()) {

            case SUCCESS -> updatePaymentStatus(
                    event.orderId(),
                    PaymentStatus.SUCCESS
            );

            case FAILED -> handlePaymentFailure(
                    event.orderId()
            );

            default -> log.info(
                    "Ignoring payment event with status :: {}",
                    event.paymentStatus()
            );
        }

        // 3. Record event as processed
        ProcessedEvent processedEvent =
                new ProcessedEvent();

        processedEvent.setEventId(
                event.eventId()
        );

        processedEvent.setProcessedAt(
                LocalDateTime.now()
        );

        processedEventRepository.save(
                processedEvent
        );

    }

    private OrderResponse mapToOrderResponse(Order order) {

        var items = order.getItems()
                .stream()
                .map(item ->
                        new OrderItemResponse(
                                item.getId(),
                                item.getProductId(),
                                item.getProductName(),
                                item.getUnitPrice(),
                                item.getQuantity(),
                                item.getUnitPrice()
                                        .multiply(
                                                java.math.BigDecimal.valueOf(
                                                        item.getQuantity()
                                                )
                                        )
                        )
                )
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getUserId(),
                order.getOrderStatus(),
                order.getPaymentStatus(),
                order.getTotalAmount(),
                items,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
