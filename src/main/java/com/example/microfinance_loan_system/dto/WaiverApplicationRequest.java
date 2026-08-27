package com.example.microfinance_loan_system.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class WaiverApplicationRequest {
    private Long emiId;
    private BigDecimal requestedAmount;
    private String hardshipDocUrl;
    private String reason;
}
