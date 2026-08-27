package com.example.microfinance_loan_system.dto;

import lombok.Data;

@Data
public class LoanRejectionRequest {
    private String rejectionCode;
    private String rejectionDetail;
}
