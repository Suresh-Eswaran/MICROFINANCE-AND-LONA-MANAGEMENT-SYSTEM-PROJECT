package com.example.microfinance_loan_system.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "security_audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(nullable = false)
    private String action;

    @Column(nullable = false)
    private String purpose; // declared audit purpose

    @Column(nullable = false)
    private LocalDateTime timestamp;
}
