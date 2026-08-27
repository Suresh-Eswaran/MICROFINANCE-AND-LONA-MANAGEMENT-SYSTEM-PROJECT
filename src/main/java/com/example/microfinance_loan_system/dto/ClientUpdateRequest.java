package com.example.microfinance_loan_system.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ClientUpdateRequest {

    private String name;

    private String phoneNumber;

    private String panNumber;

    @Min(300) @Max(900)
    private Integer cibilScore;

    private Long groupId;

    private String kycStatus; // PENDING, VERIFIED, REJECTED
}
