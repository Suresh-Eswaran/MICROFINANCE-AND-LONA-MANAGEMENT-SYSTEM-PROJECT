package com.example.microfinance_loan_system.dto;

import lombok.Data;

@Data
public class FieldNoteRequest {
    private Long clientId;
    private Long loanId;
    private String regionalLanguage;
    private String voiceTranscriptText;
}
