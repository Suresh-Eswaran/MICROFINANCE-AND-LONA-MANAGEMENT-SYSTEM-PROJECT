package com.example.microfinance_loan_system.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ReconciliationRequest {
    private Long agentId;
    private BigDecimal cashInHand;
}
