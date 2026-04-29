package com.leedsbeckett.financeservice.exception;

public class AccountNotValidException extends RuntimeException {

    public AccountNotValidException(String message) {
        super(message);
    }
}
