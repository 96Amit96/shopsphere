package com.shopsphere.orderservice.exception;

public class InventoryServiceUnavailableException extends RuntimeException{

    public InventoryServiceUnavailableException(String message) {
        super(message);
    }
}
