package com.example.microfinance_loan_system.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notification_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "recipient_id", nullable = false)
    private String recipientId; // client phone, employee email, etc.

    @Column(nullable = false)
    private String channel; // SMS, EMAIL, PUSH

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(name = "sent_timestamp", nullable = false)
    private LocalDateTime sentTimestamp;

    @Column(name = "notification_type", nullable = false)
    private String notificationType; // EMI_REMINDER, DISBURSEMENT_CONFIRM, OVERDUE_ALERT, etc.
}
