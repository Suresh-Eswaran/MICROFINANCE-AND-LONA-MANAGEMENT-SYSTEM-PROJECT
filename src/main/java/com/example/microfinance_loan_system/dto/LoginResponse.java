package com.example.microfinance_loan_system.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private String tokenType = "Bearer";
    private Long userId;
    private String email;
    private String role;

    public LoginResponse(String token) {
        this.token = token;
        this.tokenType = "Bearer";
    }

    public LoginResponse(String token, Long userId, String email, String role) {
        this.token = token;
        this.tokenType = "Bearer";
        this.userId = userId;
        this.email = email;
        this.role = role;
    }
}
