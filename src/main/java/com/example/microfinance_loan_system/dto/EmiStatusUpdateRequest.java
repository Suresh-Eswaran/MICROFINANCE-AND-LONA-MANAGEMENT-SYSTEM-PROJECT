


package com.example.microfinance_loan_system.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EmiStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private String status; // PENDING, PAID, PARTIAL, OVERDUE, WAIVED
}




