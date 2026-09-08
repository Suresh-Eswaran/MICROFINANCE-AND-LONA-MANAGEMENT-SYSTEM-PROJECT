package com.example.microfinance_loan_system.config;

import com.example.microfinance_loan_system.model.AuditLog;
import com.example.microfinance_loan_system.model.User;
import com.example.microfinance_loan_system.repository.AuditLogRepository;
import com.example.microfinance_loan_system.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class AuditLogInitializer implements CommandLineRunner {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    public AuditLogInitializer(AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        try {
            if (auditLogRepository.count() == 0) {
                log.info("Initializing security audit logs baseline...");
                List<AuditLog> initialLogs = new ArrayList<>();
                LocalDateTime now = LocalDateTime.now();

                // Baseline system logs
                initialLogs.add(AuditLog.builder()
                        .timestamp(now.minusHours(48))
                        .action("SYSTEM_INITIALIZED")
                        .email("system@microfin.internal")
                        .details("Core microfinance database and services started successfully.")
                        .success(true)
                        .build());

                initialLogs.add(AuditLog.builder()
                        .timestamp(now.minusHours(36))
                        .action("SECURITY_POLICY_APPLIED")
                        .email("system@microfin.internal")
                        .details("JWT authentication, CORS policy, and role-based access control initialized.")
                        .success(true)
                        .build());

                List<User> users = userRepository.findAll();
                int hoursOffset = 24;
                for (User user : users) {
                    initialLogs.add(AuditLog.builder()
                            .timestamp(now.minusHours(hoursOffset))
                            .action("USER_REGISTERED")
                            .userId(user.getId())
                            .email(user.getEmail())
                            .details("Account registered with role: " + user.getRole() + ", branch: " + (user.getBranch() != null ? user.getBranch() : "Head Office"))
                            .success(true)
                            .build());

                    initialLogs.add(AuditLog.builder()
                            .timestamp(now.minusHours(Math.max(0, hoursOffset - 1)))
                            .action("USER_LOGIN")
                            .userId(user.getId())
                            .email(user.getEmail())
                            .details("Successful authentication via credentials.")
                            .success(true)
                            .build());

                    hoursOffset = Math.max(1, hoursOffset - 2);
                }

                auditLogRepository.saveAll(initialLogs);
                log.info("Successfully seeded {} security audit log entries.", initialLogs.size());
            }
        } catch (Exception e) {
            log.warn("Could not initialize audit logs baseline: {}", e.getMessage());
        }
    }
}
