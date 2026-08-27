package com.example.microfinance_loan_system.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "group_meetings")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMeeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "group_id", nullable = false)
    private Long groupId;

    @Column(name = "meeting_date", nullable = false)
    private LocalDate meetingDate;

    @Column(name = "gps_lat", nullable = false, precision = 9, scale = 6)
    private BigDecimal gpsLat;

    @Column(name = "gps_lng", nullable = false, precision = 9, scale = 6)
    private BigDecimal gpsLng;

    @Column(name = "attendance_list", nullable = false, length = 1000)
    private String attendanceList; // Comma-separated client IDs

    @Column(name = "loan_officer_id", nullable = false)
    private Long loanOfficerId;
}
