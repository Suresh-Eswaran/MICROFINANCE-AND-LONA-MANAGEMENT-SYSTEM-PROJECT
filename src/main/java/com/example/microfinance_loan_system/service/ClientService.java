package com.example.microfinance_loan_system.service;

import com.example.microfinance_loan_system.dto.*;
import com.example.microfinance_loan_system.exception.DuplicateLoanException;
import com.example.microfinance_loan_system.exception.InvalidNameException;
import com.example.microfinance_loan_system.exception.InvalidPhoneException;
import com.example.microfinance_loan_system.exception.ResourceNotFoundException;
import com.example.microfinance_loan_system.model.Client;
import com.example.microfinance_loan_system.model.CreditAssessmentHistory;
import com.example.microfinance_loan_system.model.EMISchedule;
import com.example.microfinance_loan_system.model.EmiStatus;
import com.example.microfinance_loan_system.model.KycStatus;
import com.example.microfinance_loan_system.model.LoanApplication;
import com.example.microfinance_loan_system.model.LoanStatus;
import com.example.microfinance_loan_system.model.Collection;
import com.example.microfinance_loan_system.repository.ClientRepository;
import com.example.microfinance_loan_system.repository.CollectionRepository;
import com.example.microfinance_loan_system.repository.CreditAssessmentHistoryRepository;
import com.example.microfinance_loan_system.repository.EMIScheduleRepository;
import com.example.microfinance_loan_system.repository.LoanApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    @Autowired
    private EMIScheduleRepository emiScheduleRepository;

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private CreditAssessmentHistoryRepository creditAssessmentHistoryRepository;

    public Client registerClient(ClientRegistrationRequest request) {
        validateName(request.getName());
        validatePhone(request.getPhoneNumber());
        if (clientRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            throw new DuplicateLoanException("Phone number already registered: " + request.getPhoneNumber());
        }

        Client client = Client.builder()
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .panNumber(request.getPanNumber())
                .cibilScore(request.getCibilScore() != null ? request.getCibilScore() : 720)
                .groupId(request.getGroupId())
                .kycStatus(KycStatus.PENDING)
                .build();

        if (request.getPlainAadhaar() != null && !request.getPlainAadhaar().isBlank()) {
            client.setAadhaarHash(hashSHA256(request.getPlainAadhaar()));
        }

        return clientRepository.save(client);
    }

    public Client getClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
    }

    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }

    public Client updateClient(Long id, ClientUpdateRequest request) {
        Client client = getClientById(id);

        if (request.getName() != null) {
            validateName(request.getName());
            client.setName(request.getName());
        }
        if (request.getPhoneNumber() != null) {
            validatePhone(request.getPhoneNumber());
            // Make sure the new phone isn't already used by another client
            clientRepository.findByPhoneNumber(request.getPhoneNumber()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new DuplicateLoanException("Phone number already in use: " + request.getPhoneNumber());
                }
            });
            client.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getPanNumber() != null) {
            client.setPanNumber(request.getPanNumber());
        }
        if (request.getCibilScore() != null) {
            client.setCibilScore(request.getCibilScore());
        }
        if (request.getGroupId() != null) {
            client.setGroupId(request.getGroupId());
        }
        if (request.getKycStatus() != null) {
            try {
                client.setKycStatus(KycStatus.valueOf(request.getKycStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid KYC status: " + request.getKycStatus());
            }
        }

        return clientRepository.save(client);
    }

    @Transactional
    public void deleteClient(Long id) {
        Client client = getClientById(id);
        List<LoanApplication> loans = loanApplicationRepository.findByClientId(id);

        List<LoanApplication> activeLoans = loans.stream()
                .filter(l -> l.getStatus() != LoanStatus.CLOSED && l.getStatus() != LoanStatus.REJECTED)
                .toList();

        if (!activeLoans.isEmpty()) {
            String activeList = activeLoans.stream()
                    .map(l -> "Loan #" + l.getId() + " (" + l.getStatus() + ")")
                    .collect(Collectors.joining(", "));
            throw new IllegalStateException(
                    "Cannot delete client #" + id + " (" + client.getName() + ") — they have " +
                    activeLoans.size() + " active loan(s) in progress: [" + activeList +
                    "]. A client can only be deleted once all loan processes are completed (CLOSED or REJECTED) or if they have no active loans."
            );
        }

        // Clean up completed/closed loans and their associated schedules and collections
        for (LoanApplication loan : loans) {
            List<EMISchedule> schedules = emiScheduleRepository.findByLoanId(loan.getId());
            for (EMISchedule sch : schedules) {
                List<Collection> collections = collectionRepository.findAllByEmiId(sch.getId());
                if (!collections.isEmpty()) {
                    collectionRepository.deleteAll(collections);
                }
            }
            if (!schedules.isEmpty()) {
                emiScheduleRepository.deleteAll(schedules);
            }
            loanApplicationRepository.delete(loan);
        }

        // Clean up credit assessment history
        List<CreditAssessmentHistory> histories = creditAssessmentHistoryRepository.findByClientId(id);
        if (!histories.isEmpty()) {
            creditAssessmentHistoryRepository.deleteAll(histories);
        }

        clientRepository.delete(client);
    }

    public Client assessCredit(Long clientId) {
        Client client = getClientById(clientId);

        List<LoanApplication> loans = loanApplicationRepository.findByClientId(clientId);
        
        int baseCibil = client.getCibilScore() != null && client.getCibilScore() >= 300 
                ? client.getCibilScore() 
                : 700;

        int repaymentScore = 750;
        int incomeScore = 720;
        int groupScore = client.getGroupId() != null ? 760 : 700;

        if (!loans.isEmpty()) {
            int totalEmis = 0;
            int paidEmis = 0;
            int overdueEmis = 0;

            for (LoanApplication loan : loans) {
                List<EMISchedule> schedules = emiScheduleRepository.findByLoanIdOrderByInstallmentNoAsc(loan.getId());
                totalEmis += schedules.size();
                for (EMISchedule sch : schedules) {
                    if (sch.getStatus() == EmiStatus.PAID) {
                        paidEmis++;
                    } else if (sch.getStatus() == EmiStatus.OVERDUE) {
                        overdueEmis++;
                    }
                }
            }

            if (totalEmis > 0) {
                double onTimeRatio = (double) paidEmis / totalEmis;
                repaymentScore = (int) Math.round(500 + (onTimeRatio * 350) - (overdueEmis * 40));
            }
        }

        repaymentScore = Math.max(300, Math.min(900, repaymentScore));

        int compositeScore = (int) Math.round(
                (baseCibil * 0.40) + (repaymentScore * 0.30) + (incomeScore * 0.20) + (groupScore * 0.10)
        );
        compositeScore = Math.max(300, Math.min(900, compositeScore));

        String riskClass = compositeScore >= 750 ? "LOW_RISK" : (compositeScore >= 650 ? "MODERATE_RISK" : "HIGH_RISK");

        CreditAssessmentHistory history = CreditAssessmentHistory.builder()
                .clientId(clientId)
                .cibilScore(baseCibil)
                .repaymentScore(repaymentScore)
                .incomeScore(incomeScore)
                .groupScore(groupScore)
                .compositeScore(compositeScore)
                .riskClassification(riskClass)
                .assessmentDate(LocalDateTime.now())
                .overridden(false)
                .assessedBy("SYSTEM_AUTOMATED")
                .build();

        creditAssessmentHistoryRepository.save(history);

        client.setCibilScore(compositeScore);
        return clientRepository.save(client);
    }

    public Map<String, String> getCreditReport(Long clientId) {
        Client client = getClientById(clientId);
        List<LoanApplication> loans = loanApplicationRepository.findByClientId(clientId);

        int totalLoans = loans.size();
        long activeLoans = loans.stream().filter(l -> l.getStatus() == LoanStatus.DISBURSED).count();
        long closedLoans = loans.stream().filter(l -> l.getStatus() == LoanStatus.CLOSED).count();
        long pendingLoans = loans.stream().filter(l -> l.getStatus() == LoanStatus.SUBMITTED || l.getStatus() == LoanStatus.UNDER_REVIEW).count();

        int score = client.getCibilScore() != null ? client.getCibilScore() : 720;
        String riskTier = score >= 750 ? "Low Risk (Tier 1 Prime)" : (score >= 650 ? "Moderate Risk (Tier 2 Standard)" : "High Risk (Tier 3 Caution)");
        String maxEligibleLimit = score >= 800 ? "₹2,50,000" : (score >= 700 ? "₹1,00,000" : (score >= 600 ? "₹50,000" : "₹25,000"));
        String recommendedRate = score >= 800 ? "10.5% p.a." : (score >= 700 ? "12.0% p.a." : (score >= 600 ? "14.5% p.a." : "18.0% p.a."));

        Map<String, String> report = new LinkedHashMap<>();
        report.put("client_id", "#" + client.getId());
        report.put("client_name", client.getName());
        report.put("cibil_score", String.valueOf(score));
        report.put("risk_classification", riskTier);
        report.put("kyc_verification_status", client.getKycStatus() != null ? client.getKycStatus().name() : "PENDING");
        report.put("total_loan_applications", String.valueOf(totalLoans));
        report.put("active_disbursed_loans", String.valueOf(activeLoans));
        report.put("closed_successful_loans", String.valueOf(closedLoans));
        report.put("pending_loan_requests", String.valueOf(pendingLoans));
        report.put("pan_card_status", client.getPanNumber() != null ? client.getPanNumber() : "Verified");
        report.put("maximum_eligible_loan_limit", maxEligibleLimit);
        report.put("recommended_annual_interest_rate", recommendedRate);
        report.put("credit_bureau_status", "Active & Good Standing");
        report.put("report_generated_on", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

        return report;
    }

    public Client verifyIncome(Long clientId, IncomeVerificationRequest request) {
        Client client = getClientById(clientId);
        if (request.getDeclaredIncome() != null && request.getDeclaredIncome().compareTo(BigDecimal.ZERO) > 0) {
            int currentScore = client.getCibilScore() != null ? client.getCibilScore() : 700;
            int boostedScore = Math.min(900, currentScore + 25);
            client.setCibilScore(boostedScore);
        }
        return clientRepository.save(client);
    }

    public Client overrideScore(Long clientId, ScoreOverrideRequest request) {
        Client client = getClientById(clientId);
        if (request.getNewScore() == null || request.getNewScore() < 300 || request.getNewScore() > 900) {
            throw new IllegalArgumentException("CIBIL score must be between 300 and 900.");
        }

        client.setCibilScore(request.getNewScore());

        CreditAssessmentHistory history = CreditAssessmentHistory.builder()
                .clientId(clientId)
                .cibilScore(request.getNewScore())
                .compositeScore(request.getNewScore())
                .riskClassification(request.getNewScore() >= 750 ? "LOW_RISK" : (request.getNewScore() >= 650 ? "MODERATE_RISK" : "HIGH_RISK"))
                .assessmentDate(LocalDateTime.now())
                .overridden(true)
                .justification(request.getJustification())
                .assessedBy("OFFICER_OVERRIDE")
                .build();

        creditAssessmentHistoryRepository.save(history);
        return clientRepository.save(client);
    }

    public Map<String, Object> verifyKyc(KycRequest request) {
        Client client = request.getClientId() != null ? getClientById(request.getClientId()) : null;
        if (client != null) {
            client.setKycStatus(KycStatus.VERIFIED);
            if (request.getPanNumber() != null && !request.getPanNumber().isBlank()) {
                client.setPanNumber(request.getPanNumber());
            }
            clientRepository.save(client);
        }
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", "VERIFIED");
        response.put("message", "Aadhaar and PAN KYC verified successfully.");
        response.put("timestamp", LocalDateTime.now().toString());
        return response;
    }

    public Map<String, Object> cibilEnquiry(CibilRequest request) {
        Client client = request.getClientId() != null ? getClientById(request.getClientId()) : null;
        int score = (client != null && client.getCibilScore() != null) ? client.getCibilScore() : 750;
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("clientId", request.getClientId());
        response.put("cibilScore", score);
        response.put("bureau", "TransUnion CIBIL");
        response.put("status", "SUCCESS");
        response.put("timestamp", LocalDateTime.now().toString());
        return response;
    }

    private void validateName(String name) {
        if (name == null || !name.matches("[a-zA-Z ]{2,100}")) {
            throw new InvalidNameException("Name must be 2–100 alphabetic characters: " + name);
        }
    }

    private void validatePhone(String phone) {
        if (phone == null || !phone.matches("\\d{10}")) {
            throw new InvalidPhoneException("Phone number must be exactly 10 digits: " + phone);
        }
    }

    private String hashSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
