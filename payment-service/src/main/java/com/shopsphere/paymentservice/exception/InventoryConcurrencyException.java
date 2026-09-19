package com.shopsphere.paymentservice.exception;

public class InventoryConcurrencyException extends RuntimeException{

    public InventoryConcurrencyException(String message){
        super(message);
    }
}
