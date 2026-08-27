package com.example.microfinance_loan_system.model;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Entity
@Table(name = "collections")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Collection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull
    @Column(name = "emi_id", nullable = false)
    private Long emiId;
    @NotNull
    @Column(name = "collected_by", nullable = false)
    private Long collectedBy;

    @NotNull
    @DecimalMin(value = "0.01", message = "Amount collected must be greater than 0")
    @Column(name = "amount_collected", nullable = false, precision = 10, scale = 2)
    private BigDecimal amountCollected;

    @Column(name = "collection_date", nullable = false)
    @Builder.Default
    private LocalDateTime collectionDate = LocalDateTime.now();
 
    @Column(name = "gps_lat", precision = 9, scale = 6)
    private BigDecimal gpsLat;

   
    @Column(name = "gps_lng", precision = 9, scale = 6)
    private BigDecimal gpsLng;

    @Column(name = "receipt_number", nullable = false, unique = true, length = 30)
    private String receiptNumber;
}
