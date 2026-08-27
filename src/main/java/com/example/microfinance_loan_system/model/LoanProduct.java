
package com.example.microfinance_loan_system.model;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
@Entity
@Table(name = "loan_products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(name = "interest_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal interestRate;

    @Column(name = "min_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal minAmount;

    @Column(name = "max_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal maxAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "interest_method", nullable = false)
    private InterestMethod interestMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "repayment_frequency", nullable = false)
    private RepaymentFrequency repaymentFrequency;
}
