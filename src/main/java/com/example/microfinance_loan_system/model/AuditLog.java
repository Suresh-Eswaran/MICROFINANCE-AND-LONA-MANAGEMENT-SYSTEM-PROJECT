package com.example.microfinance_loan_system.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private String action;

    private Long userId;

    @Column(nullable = false)
    private String email;

    @Column(name = "ip_address", nullable = true)
    private String ipAddress;

    @Column(length = 2000)
    private String details;

    @Builder.Default
    @Column(name = "success")
    private Boolean success = true;
}
