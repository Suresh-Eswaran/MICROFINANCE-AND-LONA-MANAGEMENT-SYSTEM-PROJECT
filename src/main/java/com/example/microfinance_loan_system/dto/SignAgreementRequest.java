package com.example.microfinance_loan_system.dto;

import lombok.Data;

@Data
public class SignAgreementRequest {
    private Long loanId;
    private String signatureType; // AADHAAR_OTP, FINGERPRINT
    private String confirmationRefCode;
}
