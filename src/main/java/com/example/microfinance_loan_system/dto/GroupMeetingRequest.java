package com.example.microfinance_loan_system.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class GroupMeetingRequest {
    private Long groupId;
    private BigDecimal gpsLat;
    private BigDecimal gpsLng;
    private List<Long> presentMemberIds;
}
