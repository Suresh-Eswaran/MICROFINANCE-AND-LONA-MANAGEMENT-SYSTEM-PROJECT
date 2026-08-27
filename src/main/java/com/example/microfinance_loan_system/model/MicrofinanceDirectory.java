package com.example.microfinance_loan_system.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "microfinance_directory")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MicrofinanceDirectory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String employeeOrUserId;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String fullName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;
}
