package com.example.microfinance_loan_system.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_timelines")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoanTimeline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "loan_id", nullable = false)
    private Long loanId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoanStatus stage;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();

    @Column(name = "officer_id")
    private Long officerId;

    @Column(length = 1000)
    private String notes;
}
