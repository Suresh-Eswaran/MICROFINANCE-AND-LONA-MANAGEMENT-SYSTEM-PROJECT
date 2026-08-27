package com.example.microfinance_loan_system.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class GroupCollectionRequest {

    private Long groupId;
    private BigDecimal totalPaid;
    private Long collectedBy;
    private List<MemberAllocation> allocations;

    @Data
    public static class MemberAllocation {
        private Long clientId;
        private Long emiId;
        private BigDecimal amountAllocated;
        private BigDecimal gpsLat;
        private BigDecimal gpsLng;
    }
}
