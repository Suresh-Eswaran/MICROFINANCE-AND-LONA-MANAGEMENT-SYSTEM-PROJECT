package com.example.microfinance_loan_system.service;

import com.example.microfinance_loan_system.model.AuditLog;
import com.example.microfinance_loan_system.repository.AuditLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public void log(String action, Long userId, String email, String details, boolean success) {
        try {
            String userEmail = (email != null && !email.isBlank()) ? email : "anonymous";

            AuditLog entry = AuditLog.builder()
                    .timestamp(LocalDateTime.now())
                    .action(action)
                    .userId(userId)
                    .email(userEmail)
                    .ipAddress(null)
                    .details(details)
                    .success(success)
                    .build();

            auditLogRepository.save(entry);
        } catch (Exception e) {
            log.warn("Failed to write audit log [action={}, email={}, error={}]", action, email, e.getMessage());
        }
    }

    public void log(String action, String email, String details, boolean success) {
        log(action, null, email, details, success);
    }
}
