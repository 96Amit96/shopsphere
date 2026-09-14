package com.shopsphere.orderservice.service;

import com.shopsphere.orderservice.dto.request.CreateOrderRequest;
import com.shopsphere.orderservice.dto.response.OrderResponse;

public interface OrderService {

    OrderResponse createOrder(CreateOrderRequest request);
}
