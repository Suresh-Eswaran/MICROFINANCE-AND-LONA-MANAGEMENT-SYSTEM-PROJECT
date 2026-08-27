package com.example.microfinance_loan_system.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotBlank
    @Column(name = "full_name", nullable = false)
    private String fullName;
    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;
    @NotBlank
    @Column(nullable = false)
    private String password;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column
    private String branch;

    @Builder.Default
    @Column(nullable = false)
    private String status = "ACTIVE"; // ACTIVE, INACTIVE, SUSPENDED
}
