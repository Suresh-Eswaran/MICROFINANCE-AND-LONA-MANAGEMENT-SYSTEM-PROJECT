package com.example.microfinance_loan_system.dto;

import lombok.Data;

@Data
public class MemberExitRequest {
    private Long groupId;
    private Long exitingClientId;
    private Long targetClientId;
    private String liabilityTransferDocUrl;
}
