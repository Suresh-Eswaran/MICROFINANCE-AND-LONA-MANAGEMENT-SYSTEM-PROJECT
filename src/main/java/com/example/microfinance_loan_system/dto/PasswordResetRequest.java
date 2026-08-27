package com.example.microfinance_loan_system.dto;

import lombok.Data;

@Data
public class PasswordResetRequest {
    private String email;
    private String otp;
    private String newPassword;
}
