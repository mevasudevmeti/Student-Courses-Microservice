package com.leedsbeckett.financeservice.exception;

public class InvoiceNotValidException extends RuntimeException {

    public InvoiceNotValidException(String message) {
        super(message);
    }
}
