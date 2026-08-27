package com.example.microfinance_loan_system.dto;

import lombok.Data;

@Data
public class ProfileUpdateRequest {
    private String fullName;
    private String phone;
    private String email;
    private String assignment;
    private Boolean twoFactorEnabled;
}
