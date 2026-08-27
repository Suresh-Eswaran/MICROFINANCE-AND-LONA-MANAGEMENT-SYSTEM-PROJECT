package com.example.microfinance_loan_system.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "groups_table") // named groups_table to avoid SQL keyword "groups" conflict
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String geographicProximity;

    @Builder.Default
    private boolean trainingCompleted = false;

    @Builder.Default
    private boolean grtPassed = false;

    private java.time.LocalDate grtDate;

    @Column(length = 1000)
    private String grtAttendanceList;

    @Builder.Default
    private int trainingSessionsAttended = 0;

    @Builder.Default
    private double groupHealthScore = 100.0;
}
