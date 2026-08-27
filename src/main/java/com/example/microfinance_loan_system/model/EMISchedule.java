package com.example.microfinance_loan_system.model;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity
@Table(name = "emi_schedule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EMISchedule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "loan_id", nullable = false)
    private Long loanId;
    @Column(name = "installment_no", nullable = false)
    private Integer installmentNo;
    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;
    @Column(precision = 10, scale = 2)
    private BigDecimal principal;
    @Column(precision = 10, scale = 2)
    private BigDecimal interest;
    @Column(name = "emi_amount", precision = 10, scale = 2)
    private BigDecimal emiAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private EmiStatus status = EmiStatus.PENDING;
    /** Set when EMI is fully paid. */
    @Column(name = "paid_date")
    private LocalDateTime paidDate;
}
