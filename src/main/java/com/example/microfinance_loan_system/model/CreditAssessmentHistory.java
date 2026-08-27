package com.example.microfinance_loan_system.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_assessment_histories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditAssessmentHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(name = "cibil_score")
    private Integer cibilScore;

    @Column(name = "repayment_score")
    private Integer repaymentScore;

    @Column(name = "income_score")
    private Integer incomeScore;

    @Column(name = "group_score")
    private Integer groupScore;

    @Column(name = "composite_score")
    private Integer compositeScore;

    @Column(name = "risk_classification")
    private String riskClassification;

    @Column(name = "assessment_date", nullable = false)
    @Builder.Default
    private LocalDateTime assessmentDate = LocalDateTime.now();

    private boolean overridden;

    @Column(length = 1000)
    private String justification;

    @Column(name = "assessed_by")
    private String assessedBy;
}
