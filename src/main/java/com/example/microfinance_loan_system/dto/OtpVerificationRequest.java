package com.example.microfinance_loan_system.dto;

import lombok.Data;

@Data
public class OtpVerificationRequest {
    private String email;
    private String otp;
}
