package com.example.microfinance_loan_system.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class GroupSavingsDepositRequest {
    private Long groupId;
    private List<MemberSavingsAllocation> savingsList;

    @Data
    public static class MemberSavingsAllocation {
        private Long clientId;
        private BigDecimal amount;
    }
}
