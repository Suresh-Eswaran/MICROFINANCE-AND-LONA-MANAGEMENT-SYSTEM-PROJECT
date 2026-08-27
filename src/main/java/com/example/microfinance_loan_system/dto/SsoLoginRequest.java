package com.example.microfinance_loan_system.dto;

import lombok.Data;

@Data
public class SsoLoginRequest {
    private String ssoProvider;
    private String ssoToken;
    private String email;
}
