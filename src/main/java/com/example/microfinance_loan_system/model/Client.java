package com.example.microfinance_loan_system.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDateTime;
@Entity
@Table(name = "clients")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 100)
    private String name;
    @NotBlank
    @Column(name = "phone_number", nullable = false, length = 10)
    private String phoneNumber;
    @Column(name = "aadhaar_hash", length = 64)
    private String aadhaarHash;
    @Column(name = "pan_number", length = 10)
    private String panNumber;
    @Min(300) @Max(900)
    @Column(name = "cibil_score")
    private Integer cibilScore;
    @Column(name = "group_id")
    private Long groupId;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", nullable = false)
    @Builder.Default
    private KycStatus kycStatus = KycStatus.PENDING;

    @Column(name = "created_date", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();
}
