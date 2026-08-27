package com.example.microfinance_loan_system.dto;

import lombok.Data;

@Data
public class LegalActionRequest {
    private Long loanId;
    private String legalTeam;
    private String caseNumber;
}
