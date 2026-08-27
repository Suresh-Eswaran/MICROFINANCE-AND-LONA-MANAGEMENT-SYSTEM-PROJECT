package com.example.microfinance_loan_system.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "group_savings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupSavingsRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "client_id", nullable = false)
    private Long clientId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "deposit_date", nullable = false)
    private LocalDate depositDate;
}
