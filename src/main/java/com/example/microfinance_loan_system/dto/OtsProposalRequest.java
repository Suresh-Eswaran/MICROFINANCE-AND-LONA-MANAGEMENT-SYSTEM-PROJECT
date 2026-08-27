package com.example.microfinance_loan_system.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class OtsProposalRequest {
    private Long loanId;
    private BigDecimal settlementAmount;
    private String boardApprovalDocUrl;
}
