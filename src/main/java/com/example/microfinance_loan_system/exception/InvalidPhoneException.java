package com.example.microfinance_loan_system.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidPhoneException extends RuntimeException {
    public InvalidPhoneException(String message) {
        super(message);
    }
}
