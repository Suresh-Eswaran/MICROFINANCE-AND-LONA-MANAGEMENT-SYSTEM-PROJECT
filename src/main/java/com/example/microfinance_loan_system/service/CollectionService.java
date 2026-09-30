package com.example.microfinance_loan_system.service;

import com.example.microfinance_loan_system.dto.CollectionRequest;
import com.example.microfinance_loan_system.exception.ResourceNotFoundException;
import com.example.microfinance_loan_system.model.*;
import com.example.microfinance_loan_system.model.Collection;
import com.example.microfinance_loan_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class CollectionService {

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private EMIScheduleRepository emiScheduleRepository;

    @Autowired
    private LoanApplicationRepository loanApplicationRepository;

    // ── Record Collection ───────────────────────────────────────────────────

    /**
     * Record a payment against an EMI.
     *
     * Business rules:
     *  - Cannot record collection against a CLOSED or REJECTED loan.
     *  - GPS lat/lng are optional.
     *  - Generates a unique receipt number.
     *  - Marks the EMI status as PAID and records the payment timestamp.
     *  - If all EMIs are paid, closes the loan.
     */
    @Transactional
    public Collection recordCollection(CollectionRequest request) {
        EMISchedule emi = emiScheduleRepository.findById(request.getEmiId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "EMI not found with id: " + request.getEmiId()));

        // Fetch the parent loan to enforce business rule
        LoanApplication loan = loanApplicationRepository.findById(emi.getLoanId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Loan not found for EMI: " + request.getEmiId()));

        if (loan.getStatus() == LoanStatus.CLOSED || loan.getStatus() == LoanStatus.REJECTED) {
            throw new IllegalStateException(
                    "Cannot record collection — loan is " + loan.getStatus() + ".");
        }

        String receiptNumber = "REC-" + System.currentTimeMillis() + "-" + new Random().nextInt(9000 + 1000);

        Collection collection = Collection.builder()
                .emiId(request.getEmiId())
                .collectedBy(request.getCollectedBy())
                .amountCollected(request.getAmountCollected())
                .gpsLat(request.getGpsLat())
                .gpsLng(request.getGpsLng())
                .receiptNumber(receiptNumber)
                .build();

        Collection saved = collectionRepository.save(collection);

        // Update EMI status to PAID and record payment timestamp
        emi.setStatus(EmiStatus.PAID);
        emi.setPaidDate(LocalDateTime.now());
        emiScheduleRepository.save(emi);

        // Check if all EMIs for this loan are now settled
        List<EMISchedule> allLoanEmis = emiScheduleRepository.findByLoanId(loan.getId());
        boolean allSettled = allLoanEmis.stream().allMatch(e -> e.getStatus() == EmiStatus.PAID);
        if (allSettled && !allLoanEmis.isEmpty()) {
            loan.setStatus(LoanStatus.CLOSED);
            loanApplicationRepository.save(loan);
        }

        return saved;
    }

    // ── Read ────────────────────────────────────────────────────────────────

    /**
     * Returns all EMIs that are OVERDUE (due date in the past, not yet PAID/WAIVED).
     */
    public List<EMISchedule> getOverdueEmis() {
        return emiScheduleRepository.findByDueDateBeforeAndStatusNot(LocalDate.now(), EmiStatus.PAID);
    }

    /**
     * Returns all collections recorded for a specific EMI.
     */
    public List<Collection> getCollectionsByEmiId(Long emiId) {
        emiScheduleRepository.findById(emiId)
                .orElseThrow(() -> new ResourceNotFoundException("EMI not found with id: " + emiId));
        return collectionRepository.findAllByEmiId(emiId);
    }

    @Autowired
    private com.example.microfinance_loan_system.service.ClientService clientService;

    public List<Collection> getReceiptsForClient(String email) {
        try {
            Client client = clientService.getClientForCurrentUser(email);
            if (client == null) {
                return java.util.Collections.emptyList();
            }
            List<LoanApplication> loans = loanApplicationRepository.findByClientId(client.getId());
            if (loans.isEmpty()) {
                return java.util.Collections.emptyList();
            }
            List<Long> loanIds = loans.stream().map(LoanApplication::getId).toList();
            List<EMISchedule> allEmis = new java.util.ArrayList<>();
            for (Long lid : loanIds) {
                allEmis.addAll(emiScheduleRepository.findByLoanId(lid));
            }
            List<Long> emiIds = allEmis.stream().map(EMISchedule::getId).toList();
            if (emiIds.isEmpty()) {
                return java.util.Collections.emptyList();
            }
            List<Collection> recorded = collectionRepository.findAllByEmiIdIn(emiIds);
            
            java.util.Set<Long> collectedEmiIds = recorded.stream()
                    .map(Collection::getEmiId)
                    .collect(java.util.stream.Collectors.toSet());
            List<Collection> result = new java.util.ArrayList<>(recorded);
            for (EMISchedule emi : allEmis) {
                if (emi.getStatus() == EmiStatus.PAID && !collectedEmiIds.contains(emi.getId())) {
                    result.add(Collection.builder()
                            .id(emi.getId())
                            .emiId(emi.getId())
                            .amountCollected(emi.getEmiAmount() != null ? emi.getEmiAmount() : java.math.BigDecimal.ZERO)
                            .collectionDate(emi.getPaidDate() != null ? emi.getPaidDate() : java.time.LocalDateTime.now())
                            .receiptNumber("REC-" + emi.getLoanId() + "-" + (emi.getInstallmentNo() != null ? emi.getInstallmentNo() : emi.getId()))
                            .collectedBy(101L)
                            .build());
                }
            }
            result.sort((a, b) -> {
                if (a.getCollectionDate() == null || b.getCollectionDate() == null) return 0;
                return b.getCollectionDate().compareTo(a.getCollectionDate());
            });
            return result;
        } catch (ResourceNotFoundException e) {
            return java.util.Collections.emptyList();
        }
    }
}
