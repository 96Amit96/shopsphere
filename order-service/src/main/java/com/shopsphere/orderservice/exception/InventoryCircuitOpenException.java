package com.shopsphere.orderservice.exception;

public class InventoryCircuitOpenException extends RuntimeException{

    public InventoryCircuitOpenException(String message) {
        super(message);
    }
}
