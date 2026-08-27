package com.example.microfinance_loan_system.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "user_devices")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDevice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String deviceFingerprint;

    @Builder.Default
    private boolean verified = false;
}
