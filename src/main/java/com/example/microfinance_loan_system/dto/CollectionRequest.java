package com.example.microfinance_loan_system.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CollectionRequest {

    @NotNull(message = "EMI ID is required")
    private Long emiId;

    @NotNull(message = "Amount collected is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amountCollected;

    @NotNull(message = "collectedBy (user ID) is required")
    private Long collectedBy;

    /** Optional GPS latitude. */
    private BigDecimal gpsLat;

    /** Optional GPS longitude. */
    private BigDecimal gpsLng;
}




