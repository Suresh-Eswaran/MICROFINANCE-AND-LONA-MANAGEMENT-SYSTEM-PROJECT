package com.example.microfinance_loan_system.dto;

import lombok.Data;
import java.util.List;

@Data
public class OfflineSyncRequest {
    private List<CollectionRequest> collections;
}
