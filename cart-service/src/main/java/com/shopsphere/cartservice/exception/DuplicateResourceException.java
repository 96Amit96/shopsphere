package com.shopsphere.cartservice.exception;

public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message){
        super(message);
    }
}
