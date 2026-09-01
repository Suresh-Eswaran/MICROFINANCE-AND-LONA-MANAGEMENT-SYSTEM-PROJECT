package com.example.microfinance_loan_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Email(message = "Valid email is required")
    @NotBlank
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotNull(message = "Role is required")
    private String role; // ADMIN, BRANCH_MANAGER, CREDIT_OFFICER, LOAN_OFFICER, COLLECTIONS_AGENT, CLIENT

    @NotBlank(message = "Branch is required")
    private String branch;
}
