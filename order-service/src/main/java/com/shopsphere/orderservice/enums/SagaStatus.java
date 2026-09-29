package com.shopsphere.orderservice.enums;

public enum SagaStatus {

    STARTED,

    INVENTORY_RESERVATION_PENDING,
    INVENTORY_RESERVED,

    PAYMENT_PENDING,
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,

    COMPENSATING,
    COMPLETED,
    FAILED
}
