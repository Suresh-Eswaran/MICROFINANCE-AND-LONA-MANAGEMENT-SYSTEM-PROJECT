
package com.example.microfinance_loan_system.service;
import com.example.microfinance_loan_system.exception.ResourceNotFoundException;
import com.example.microfinance_loan_system.model.*;
import com.example.microfinance_loan_system.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class EmiService {

    private final EMIScheduleRepository emiScheduleRepository;

    private final LoanApplicationRepository loanApplicationRepository;

    private final CollectionRepository collectionRepository;


    EmiService(EMIScheduleRepository emiScheduleRepository, LoanApplicationRepository loanApplicationRepository, CollectionRepository collectionRepository) {
        this.emiScheduleRepository = emiScheduleRepository;
        this.loanApplicationRepository = loanApplicationRepository;
        this.collectionRepository = collectionRepository;
    }

   
    @Transactional
    public List<EMISchedule> generateSchedule(Long loanId) {
        LoanApplication loan = loanApplicationRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + loanId));

        if (loan.getStatus() != LoanStatus.DISBURSED) {
            throw new IllegalStateException(
                    "EMI schedule can only be generated for DISBURSED loans. Current status: " + loan.getStatus());
        }

        if (loan.getTenureMonths() == null || loan.getAnnualInterestRate() == null) {
            throw new IllegalStateException(
                    "Loan is missing tenure_months or annual_interest_rate — cannot generate EMI schedule.");
        }

         
        List<EMISchedule> existing = emiScheduleRepository.findByLoanId(loanId);
        if (!existing.isEmpty()) {
            emiScheduleRepository.deleteAll(existing);
        }

        BigDecimal principal = loan.getAmountRequested();
        int tenure = loan.getTenureMonths();
        BigDecimal annualRate = loan.getAnnualInterestRate();
 
        BigDecimal monthlyInterest = principal
                .multiply(annualRate)
                .divide(BigDecimal.valueOf(100 * 12), 2, RoundingMode.HALF_UP);

        
        BigDecimal principalPerInstallment = principal
                .divide(BigDecimal.valueOf(tenure), 2, RoundingMode.HALF_UP);

         
        BigDecimal emiAmount = principalPerInstallment.add(monthlyInterest);

        List<EMISchedule> schedule = new ArrayList<>();
        LocalDate dueDate = LocalDate.now().plusMonths(1);

        for (int i = 1; i <= tenure; i++) {
            EMISchedule emi = EMISchedule.builder()
                    .loanId(loanId)
                    .installmentNo(i)
                    .dueDate(dueDate)
                    .principal(principalPerInstallment)
                    .interest(monthlyInterest)
                    .emiAmount(emiAmount)
                    .status(EmiStatus.PENDING)
                    .build();

            schedule.add(emi);
            dueDate = dueDate.plusMonths(1);
        }

        return emiScheduleRepository.saveAll(schedule);
    }

    

    public List<EMISchedule> getScheduleByLoanId(Long loanId) {
        // Ensure loan exists
        loanApplicationRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + loanId));
        return emiScheduleRepository.findByLoanIdOrderByInstallmentNoAsc(loanId);
    }
 
    @Transactional
    public EMISchedule updateEmiStatus(Long emiId, String statusStr) {
        EMISchedule emi = emiScheduleRepository.findById(emiId)
                .orElseThrow(() -> new ResourceNotFoundException("EMI not found with id: " + emiId));

        EmiStatus newStatus;
        try {
            newStatus = EmiStatus.valueOf(statusStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid EMI status: " + statusStr +
                    ". Valid: PENDING, PAID, PARTIAL, OVERDUE, WAIVED");
        }

        if (newStatus == EmiStatus.PAID) {
            // Sum all collections for this EMI
            BigDecimal totalCollected = collectionRepository.findAllByEmiId(emiId).stream()
                    .map(c -> c.getAmountCollected())
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            if (totalCollected.compareTo(emi.getEmiAmount()) < 0) {
                throw new IllegalStateException(
                        "Cannot mark EMI as PAID — total collected (" + totalCollected +
                        ") is less than EMI amount (" + emi.getEmiAmount() + ").");
            }
            emi.setPaidDate(LocalDateTime.now());
        }

        emi.setStatus(newStatus);
        return emiScheduleRepository.save(emi);
    }
}
