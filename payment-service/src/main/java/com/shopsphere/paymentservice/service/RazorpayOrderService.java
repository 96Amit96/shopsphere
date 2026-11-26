package com.shopsphere.paymentservice.service;

import com.shopsphere.paymentservice.dto.response.RazorpayOrderResponse;

public interface RazorpayOrderService {
    RazorpayOrderResponse createRazorpayOrder(Long paymentId);
}
