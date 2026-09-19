package com.shopsphere.paymentservice.mapper;

import com.shopsphere.paymentservice.dto.response.PaymentResponse;
import com.shopsphere.paymentservice.entity.Payment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PaymentMapper {
    PaymentResponse toPaymentResponse(Payment payment);
}
