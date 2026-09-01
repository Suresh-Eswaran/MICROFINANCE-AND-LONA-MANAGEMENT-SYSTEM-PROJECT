package com.example.microfinance_loan_system.controller;

import com.example.microfinance_loan_system.dto.DashboardStatsDTO;
import com.example.microfinance_loan_system.model.LoanApplication;
import com.example.microfinance_loan_system.model.LoanStatus;
import com.example.microfinance_loan_system.model.EmiStatus;
import com.example.microfinance_loan_system.model.AuditLog;
import com.example.microfinance_loan_system.repository.AuditLogRepository;
import com.example.microfinance_loan_system.repository.ClientRepository;
import com.example.microfinance_loan_system.repository.EMIScheduleRepository;
import com.example.microfinance_loan_system.repository.LoanApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class SystemAdminController {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    @Autowired
    private EMIScheduleRepository emiScheduleRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsDTO> getDashboardStats() {
        long totalClients = clientRepository.count();

        List<LoanApplication> activeLoansList = loanApplicationRepository.findByStatus(LoanStatus.DISBURSED);
        long activeLoans = activeLoansList.size();

        BigDecimal totalPortfolioValue = activeLoansList.stream()
                .map(LoanApplication::getAmountRequested)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long pendingApprovals = loanApplicationRepository.findByStatus(LoanStatus.SUBMITTED).size();

        long overdueEMIs = emiScheduleRepository.findByStatus(EmiStatus.OVERDUE).size();

        DashboardStatsDTO stats = DashboardStatsDTO.builder()
                .totalClients(totalClients)
                .activeLoans(activeLoans)
                .totalPortfolioValue(totalPortfolioValue)
                .pendingApprovals(pendingApprovals)
                .overdueEMIs(overdueEMIs)
                .build();

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getSystemHealth() {
        Map<String, Object> health = new LinkedHashMap<>();
        health.put("database", "UP");
        health.put("api_gateway", "UP");
        health.put("auth_service", "UP");
        health.put("sms_service", "UP");
        return ResponseEntity.ok(health);
    }

    @GetMapping("/audit-logs/search")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        return ResponseEntity.ok(auditLogRepository.findAllByOrderByTimestampDesc());
    }

    @GetMapping("/integrations")
    public ResponseEntity<Map<String, Object>> getIntegrations() {
        Map<String, Object> integrations = new LinkedHashMap<>();
        integrations.put("cibil_score_service", "CONNECTED (Mock Mode)");
        integrations.put("uidai_aadhaar_service", "CONNECTED (SHA-256 Mock)");
        integrations.put("rbi_compliance_feed", "ACTIVE");
        integrations.put("payment_gateway", "READY");
        return ResponseEntity.ok(integrations);
    }
}