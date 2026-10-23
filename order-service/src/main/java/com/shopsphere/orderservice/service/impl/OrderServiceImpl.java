package com.shopsphere.orderservice.service.impl;

import com.shopsphere.orderservice.client.CartClient;
import com.shopsphere.orderservice.client.UserClient;
import com.shopsphere.orderservice.dto.event.*;
import com.shopsphere.orderservice.dto.request.CreateOrderRequest;
import com.shopsphere.orderservice.dto.response.*;
import com.shopsphere.orderservice.entity.Order;
import com.shopsphere.orderservice.entity.OrderItem;
import com.shopsphere.orderservice.entity.ProcessedEvent;
import com.shopsphere.orderservice.enums.OrderStatus;
import com.shopsphere.orderservice.enums.PaymentStatus;
import com.shopsphere.orderservice.enums.SagaStatus;
import com.shopsphere.orderservice.exception.ResourceNotFoundException;
import com.shopsphere.orderservice.kafka.InventoryCompensationEventProducer;
import com.shopsphere.orderservice.kafka.InventoryDeductionEventProducer;
import com.shopsphere.orderservice.kafka.OrderEventProducer;
import com.shopsphere.orderservice.repository.OrderRepository;
import com.shopsphere.orderservice.repository.ProcessedEventRepository;
import com.shopsphere.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserClient userClient;
    private final CartClient cartClient;
    private final ProcessedEventRepository processedEventRepository;
    private final OrderEventProducer orderEventProducer;
    private final InventoryCompensationEventProducer inventoryCompensationEventProducer;
    private final InventoryDeductionEventProducer inventoryDeductionEventProducer;

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

        order.setPaymentMethod(
                request.paymentMethod().trim().toUpperCase(Locale.ROOT)
        );

        order.setSagaStatus(SagaStatus.INVENTORY_RESERVATION_PENDING);
        // 7. Save Order
        Order savedOrder = orderRepository.save(order);
        log.info("Saved order :: {}", savedOrder.getId());

        List<OrderItemEvent> eventItems = savedOrder.getItems()
                .stream()
                .map(
                        item -> new OrderItemEvent(
                                item.getProductId(),
                                item.getProductName(),
                                item.getUnitPrice(),
                                item.getQuantity()
                        )
                ).toList();

        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.randomUUID(),
                savedOrder.getId(),
                savedOrder.getUserId(),
                savedOrder.getTotalAmount(),eventItems
        );

        orderEventProducer.publishOrderCreated(event);

        log.info(
                "OrderCreatedEvent published for orderId :: {}",
                savedOrder.getId()
        );

        // 8. Map response
        return mapToOrderResponse(savedOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders() {

        ApiResponse<CurrentUserResponse> currentUserResponse =  userClient.getCurrentUser();

        CurrentUserResponse currentUser = currentUserResponse.data();

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
                    "Payment failure already handled for orderId or compensation pending"+ "orderId :: {} , sagaStatus :: {}",
                    orderId,
                    order.getSagaStatus()
            );

            return mapToOrderResponse(order);
        }
        // 1. Update state before requesting compensation.

        order.setPaymentStatus(PaymentStatus.FAILED);
        order.setOrderStatus(OrderStatus.CANCELLED);
        order.setSagaStatus(SagaStatus.COMPENSATING);

        Order updatedOrder =
                orderRepository.save(order);

        // 2. Build the compensation items from the order.

        List<InventoryCompensationItemEvent> compensationItems=
                updatedOrder.getItems()
                        .stream()
                        .map(item ->
                                new InventoryCompensationItemEvent(
                                        item.getProductId(),
                                        item.getQuantity()
                                )
                        )
                        .toList();

        // 3. Create the Kafka event.

        InventoryCompensationRequestedEvent compensationEvent =
                new InventoryCompensationRequestedEvent(
                        UUID.randomUUID(),
                        updatedOrder.getId(),
                        updatedOrder.getUserId(),
                        compensationItems,
                        "PAYMENT_FAILED",
                        LocalDateTime.now()
                );

        // 4. Publish only after the order transaction commits.
        // This prevents Inventory Service from responding before
        // the COMPENSATING state has been persisted.

       inventoryCompensationEventProducer.publishCompensationRequested(compensationEvent);

        log.info(
                "Inventory compensation request saved to outbox for orderId :: {}",
                order.getId()
        );

        return mapToOrderResponse(updatedOrder);
    }


    private void handlePaymentSuccess(PaymentEvent event) {

        log.info(
                "Handling payment success for orderId :: {}",
                event.orderId()
        );

        Order order = orderRepository.findById(event.orderId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Order not found with id :: " + event.orderId()
                        )
                );

        // Do not finalize an order that has already been cancelled
        // or is undergoing compensation.
        if (order.getOrderStatus() == OrderStatus.CANCELLED
                || order.getSagaStatus() == SagaStatus.COMPENSATING
                || order.getSagaStatus() == SagaStatus.FAILED) {

            throw new IllegalStateException(
                    "Cannot finalize a cancelled or failed order. orderId :: "
                            + event.orderId()
            );
        }

        // Avoid scheduling another deduction for an already completed
        // order or a payment-success event already being handled.
        if (order.getSagaStatus() == SagaStatus.COMPLETED
                || (order.getPaymentStatus() == PaymentStatus.SUCCESS
                && order.getSagaStatus() == SagaStatus.PAYMENT_SUCCESS)) {

            log.info(
                    "Payment success already handled. orderId :: {}, sagaStatus :: {}",
                    order.getId(),
                    order.getSagaStatus()
            );

            return;
        }

        // The inventory reservation must have completed before payment.
        if (order.getSagaStatus() != SagaStatus.PAYMENT_PENDING) {
            throw new IllegalStateException(
                    "Order is not awaiting payment. orderId :: "
                            + order.getId()
                            + ", sagaStatus :: "
                            + order.getSagaStatus()
            );
        }

        // Record payment success, but don't confirm the order yet.
        order.setPaymentStatus(PaymentStatus.SUCCESS);
        order.setSagaStatus(SagaStatus.PAYMENT_SUCCESS);

        Order savedOrder = orderRepository.save(order);

        List<InventoryDeductionItemEvent> items =
                savedOrder.getItems()
                        .stream()
                        .map(item ->
                                new InventoryDeductionItemEvent(
                                        item.getProductId(),
                                        item.getQuantity()
                                )
                        )
                        .toList();

        InventoryDeductionRequestedEvent deductionEvent =
                new InventoryDeductionRequestedEvent(
                        UUID.randomUUID(),
                        event.eventId(),
                        savedOrder.getId(),
                        savedOrder.getUserId(),
                        items,
                        LocalDateTime.now()
                );

        // Save the deduction request to the outbox within the current transaction.
        inventoryDeductionEventProducer.publishDeductionRequested(deductionEvent);

        log.info(
                "Payment succeeded. Inventory deduction request saved to outbox for orderId :: {}",
                savedOrder.getId()
        );
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

            case SUCCESS -> handlePaymentSuccess(event);

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
                order.getPaymentMethod(),
                order.getSagaStatus(),
                order.getTotalAmount(),
                items,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
