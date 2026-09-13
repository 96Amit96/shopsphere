package com.shopsphere.orderservice.exception;

public class InventoryConcurrencyException extends RuntimeException{

    public InventoryConcurrencyException(String message){
        super(message);
    }
}
