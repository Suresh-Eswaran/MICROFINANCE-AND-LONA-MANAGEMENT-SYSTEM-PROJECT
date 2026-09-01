package com.example.microfinance_loan_system.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStatsDTO {
    private BigDecimal totalPortfolioValue;
    private long activeLoans;
    private long totalClients;
    private long overdueEMIs;
    private long pendingApprovals;
}
