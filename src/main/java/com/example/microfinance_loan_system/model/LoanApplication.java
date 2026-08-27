package com.example.microfinance_loan_system.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_applications")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "client_id", nullable = false)
    private Long clientId;

    /** Nullable FK — LoanProducts table not implemented in this scope. */
    @Column(name = "product_id")
    private Long productId;

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    @Column(name = "amount_requested", nullable = false, precision = 12, scale = 2)
    private BigDecimal amountRequested;

    @Column(length = 200)
    private String purpose;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private LoanStatus status = LoanStatus.SUBMITTED;

    /** Required before approval (validated in service). */
    @Column(name = "officer_id")
    private Long officerId;

    @Column(name = "applied_date", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime appliedDate = LocalDateTime.now();

    /**
     * Loan tenure in months — used for EMI schedule generation.
     */
    @Column(name = "tenure_months")
    private Integer tenureMonths;

    /**
     * Annual interest rate in percent (e.g. 12.5 for 12.5%) — used for EMI generation.
     */
    @DecimalMin(value = "0.0")
    @Column(name = "annual_interest_rate", precision = 5, scale = 2)
    private BigDecimal annualInterestRate;
}
