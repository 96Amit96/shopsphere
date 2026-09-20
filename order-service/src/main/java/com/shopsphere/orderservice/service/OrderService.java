package com.shopsphere.orderservice.service;

import com.shopsphere.orderservice.dto.request.CreateOrderRequest;
import com.shopsphere.orderservice.dto.response.OrderResponse;
import com.shopsphere.orderservice.enums.PaymentStatus;

import java.util.List;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);

    List<OrderResponse> getMyOrders();

    OrderResponse getMyOrder(Long orderId);

    OrderResponse updatePaymentStatus(
            Long orderId,
            PaymentStatus paymentStatus
    );

    OrderResponse handlePaymentFailure(Long orderId);
}
