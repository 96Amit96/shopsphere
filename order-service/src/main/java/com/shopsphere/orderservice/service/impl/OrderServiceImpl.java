package com.shopsphere.orderservice.service.impl;

import com.shopsphere.orderservice.client.CartClient;
import com.shopsphere.orderservice.client.UserClient;
import com.shopsphere.orderservice.dto.request.CreateOrderRequest;
import com.shopsphere.orderservice.dto.response.*;
import com.shopsphere.orderservice.entity.Order;
import com.shopsphere.orderservice.entity.OrderItem;
import com.shopsphere.orderservice.repository.OrderRepository;
import com.shopsphere.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final UserClient userClient;
    private final CartClient cartClient;

    @Override
    @Transactional
    public OrderResponse createOrder(CreateOrderRequest request) {

        log.info("Create order request :: {}", request );

        // 1. Get authenticated user
        ApiResponse<CurrentUserResponse> currentUserResponse
                = userClient.getCurrentUser();
        log.info("Current user :: {}", currentUserResponse.data());
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

        // 8. Map response
        return mapToOrderResponse(savedOrder);
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
