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

        return collectionRepository.save(collection);
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
}
