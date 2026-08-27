package com.example.microfinance_loan_system.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "compliance_calendar")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ComplianceCalendar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "activity_name", nullable = false)
    private String activityName;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(name = "submission_authority", nullable = false)
    private String submissionAuthority; // RBI, CIBIL, MFIN

    @Column(name = "alert_days", nullable = false)
    @Builder.Default
    private int alertDays = 15;

    @Column(nullable = false)
    @Builder.Default
    private String status = "PENDING"; // PENDING, COMPLETED
}
