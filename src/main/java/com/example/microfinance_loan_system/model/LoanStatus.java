package com.example.microfinance_loan_system.model;

/**
 * Loan application lifecycle statuses per the university spec.
 */
public enum LoanStatus {
    SUBMITTED,
    UNDER_REVIEW,
    APPROVED,
    REJECTED,
    DISBURSED,
    CLOSED,
    NPA
}
