package com.example.microfinance_loan_system.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "permission_matrix")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PermissionMatrix {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private String functionName;

    @Builder.Default
    private boolean enabled = true;
}
