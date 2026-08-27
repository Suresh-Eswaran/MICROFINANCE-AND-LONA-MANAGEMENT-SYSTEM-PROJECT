package com.example.microfinance_loan_system.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateLoanException extends RuntimeException {
    public DuplicateLoanException(String message) {
        super(message);
    }
}
