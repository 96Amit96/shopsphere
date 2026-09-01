package com.shopsphere.inventoryservice.exception;

public class InventoryConcurrencyException extends RuntimeException{

    public InventoryConcurrencyException(String message){
        super(message);
    }
}
