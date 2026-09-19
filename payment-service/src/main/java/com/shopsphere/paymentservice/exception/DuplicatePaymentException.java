package com.shopsphere.paymentservice.exception;

public class DuplicatePaymentException extends RuntimeException{

    public DuplicatePaymentException(String message) {
        super(message);
    }
}
