package com.shopsphere.cartservice.exception;

public class InventoryConcurrencyException extends RuntimeException{

    public InventoryConcurrencyException(String message){
        super(message);
    }
}
