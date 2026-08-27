package com.example.microfinance_loan_system.dto;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
@Data
public class LoanApplyRequest {

    @NotNull(message = "Client ID is required")
    private Long clientId;

    /** Nullable — no LoanProducts table implemented. */
    private Long productId;

    @NotNull(message = "Amount requested is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amountRequested;

    @NotBlank(message = "Purpose is required")
    private String purpose;

    /** Officer must be assigned before approval is possible. */
    private Long officerId;

    @NotNull(message = "Tenure months is required")
    @Min(value = 1, message = "Tenure must be at least 1 month")
    private Integer tenureMonths;

    @NotNull(message = "Annual interest rate is required")
    @DecimalMin(value = "0.0", message = "Interest rate cannot be negative")
    private BigDecimal annualInterestRate;
}
