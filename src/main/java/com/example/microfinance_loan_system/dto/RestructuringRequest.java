package com.example.microfinance_loan_system.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RestructuringRequest {
    private String justification;
    private BigDecimal newInterestRate;
    private int newInstallments;
}
