package com.example.microfinance_loan_system.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @Email(message = "Valid email is required")
    @NotBlank
    private String email;

    @NotBlank(message = "Password is required")
    private String password;
}
