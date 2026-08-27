package com.example.microfinance_loan_system.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class IncomeVerificationRequest {
    private BigDecimal declaredIncome;
    private String borrowerType; // SALARIED, SELF_EMPLOYED
    private boolean salaried;
    private String bankStatementOcrUrl;
    private Integer businessVintageYears;
    private BigDecimal annualTurnover;
}
