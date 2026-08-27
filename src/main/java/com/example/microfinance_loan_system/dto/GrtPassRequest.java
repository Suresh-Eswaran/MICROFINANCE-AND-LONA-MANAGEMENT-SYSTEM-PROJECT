package com.example.microfinance_loan_system.dto;

import lombok.Data;
import java.util.List;

@Data
public class GrtPassRequest {
    private List<Long> presentMemberIds;
}
