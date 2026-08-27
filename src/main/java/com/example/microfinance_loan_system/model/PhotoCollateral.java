package com.example.microfinance_loan_system.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "photo_collaterals")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PhotoCollateral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "loan_id", nullable = false)
    private Long loanId;

    @Column(name = "photo_type", nullable = false)
    private String photoType; // PREMISES, COLLATERAL

    @Column(name = "photo_url", nullable = false)
    private String photoUrl;

    @Column(name = "gps_lat", precision = 9, scale = 6)
    private BigDecimal gpsLat;

    @Column(name = "gps_lng", precision = 9, scale = 6)
    private BigDecimal gpsLng;
}
