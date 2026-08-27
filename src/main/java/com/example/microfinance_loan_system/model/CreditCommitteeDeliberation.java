package com.example.microfinance_loan_system.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "credit_committee_deliberations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditCommitteeDeliberation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "loan_id", nullable = false)
    private Long loanId;

    @Column(name = "officer_sign_offs", length = 1000)
    private String officerSignOffs; // Comma separated user emails or usernames

    @Column(nullable = false)
    @Builder.Default
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @Column(length = 2000)
    private String justification;

    @Column(name = "created_date", nullable = false)
    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();
}
