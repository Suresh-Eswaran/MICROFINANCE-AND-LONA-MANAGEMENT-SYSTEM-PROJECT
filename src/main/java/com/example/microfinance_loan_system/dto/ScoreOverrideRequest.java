package com.example.microfinance_loan_system.dto;

import lombok.Data;

@Data
public class ScoreOverrideRequest {
    private Integer newScore;
    private String justification;
}
