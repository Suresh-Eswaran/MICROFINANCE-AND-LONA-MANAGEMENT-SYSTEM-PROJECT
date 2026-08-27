

package com.example.microfinance_loan_system.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ClientRegistrationRequest {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    /** Raw Aadhaar number — will be hashed (SHA-256) before storage. */
    private String plainAadhaar;

    private String panNumber;

    @Min(300) @Max(900)
    private Integer cibilScore;

    private Long groupId;
}
