package com.example.microfinance_loan_system.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class WriteOffRecommendationResponse {
    private Long loanId;
    private String borrowerName;
    private BigDecimal outstandingAmount;
    private BigDecimal recoveryEstimate; // e.g. 5% of outstanding
    private BigDecimal collectionCostEstimate; // e.g. 2% of outstanding
    private String reason;
}
