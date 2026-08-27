package com.example.microfinance_loan_system.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "field_visits")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FieldVisit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "agent_id", nullable = false)
    private Long agentId;

    @Column(name = "client_id")
    private Long clientId;

    @Column(name = "visit_timestamp", nullable = false)
    private LocalDateTime visitTimestamp;

    @Column(name = "gps_lat", nullable = false, precision = 9, scale = 6)
    private BigDecimal gpsLat;

    @Column(name = "gps_lng", nullable = false, precision = 9, scale = 6)
    private BigDecimal gpsLng;

    @Column(nullable = false)
    private String purpose; // REGISTRATION, COLLECTION, PREMISES_VERIFICATION, INQUIRY
}
