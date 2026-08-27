package com.example.microfinance_loan_system.dto;

import lombok.Data;

@Data
public class AuditLogRequest {
    private String action;
    private String purpose;
}
