package com.example.microfinance_loan_system.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "waiver_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WaiverRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "emi_id", nullable = false)
    private Long emiId;

    @Column(name = "requested_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal requestedAmount;

    @Column(name = "hardship_doc_url", nullable = false)
    private String hardshipDocUrl;

    @Column(nullable = false, length = 1000)
    private String reason;

    @Column(nullable = false)
    @Builder.Default
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "created_date", nullable = false)
    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();
}
