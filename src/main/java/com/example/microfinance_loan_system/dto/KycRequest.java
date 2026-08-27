package com.example.microfinance_loan_system.dto;

import lombok.Data;

@Data
public class KycRequest {
    private Long clientId;
    private String aadhaarOtp;
    private String panNumber;
    private String name;
    private java.time.LocalDate dob;
}
