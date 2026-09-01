
package com.example.microfinance_loan_system.service;
import com.example.microfinance_loan_system.dto.LoanApplyRequest;
import com.example.microfinance_loan_system.exception.ResourceNotFoundException;
import com.example.microfinance_loan_system.model.*;
import com.example.microfinance_loan_system.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service
public class LoanService {
    @Autowired
    private LoanApplicationRepository loanApplicationRepository;
    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private EMIScheduleRepository emiScheduleRepository;
    @Autowired
    private CollectionRepository collectionRepository;
    public LoanApplication applyForLoan(LoanApplyRequest request) {
        Client client = clientRepository.findById(request.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client not found with id: " + request.getClientId()));

        if (client.getKycStatus() != KycStatus.VERIFIED) {
            throw new IllegalStateException(
                    "Client KYC must be VERIFIED before a loan application can be submitted. " +
                    "Current status: " + client.getKycStatus());
        }
        if (request.getOfficerId() != null) {
            userRepository.findById(request.getOfficerId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Officer not found with id: " + request.getOfficerId()));
        }

        LoanApplication loan = LoanApplication.builder()
                .clientId(request.getClientId())
                .productId(request.getProductId())
                .amountRequested(request.getAmountRequested())
                .purpose(request.getPurpose())
                .officerId(request.getOfficerId())
                .tenureMonths(request.getTenureMonths())
                .annualInterestRate(request.getAnnualInterestRate())
                .status(LoanStatus.SUBMITTED)
                .build();

        return loanApplicationRepository.save(loan);
    } 

    public LoanApplication getLoanById(Long id) {
        return loanApplicationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found with id: " + id));
    }

    public List<LoanApplication> getLoansByClientId(Long clientId) {
        // Verify client exists
        clientRepository.findById(clientId)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + clientId));
        return loanApplicationRepository.findByClientId(clientId);
    }

    public List<LoanApplication> getAllLoans() {
        return loanApplicationRepository.findAll();
    }
    @Transactional
    public LoanApplication approveLoan(Long id) {
        LoanApplication loan = getLoanById(id);

        if (loan.getOfficerId() == null) {
            throw new IllegalStateException(
                    "Cannot approve loan " + id + " — no officer assigned. Set officer_id first.");
        }

        if (loan.getStatus() != LoanStatus.SUBMITTED && loan.getStatus() != LoanStatus.UNDER_REVIEW) {
            throw new IllegalStateException(
                    "Cannot approve loan in status: " + loan.getStatus() +
                    ". Loan must be SUBMITTED or UNDER_REVIEW.");
        }

        loan.setStatus(LoanStatus.APPROVED);
        return loanApplicationRepository.save(loan);
    }
    @Transactional
    public LoanApplication rejectLoan(Long id) {
        LoanApplication loan = getLoanById(id);

        if (loan.getStatus() == LoanStatus.DISBURSED || loan.getStatus() == LoanStatus.CLOSED) {
            throw new IllegalStateException(
                    "Cannot reject loan in status: " + loan.getStatus());
        }

        loan.setStatus(LoanStatus.REJECTED);
        return loanApplicationRepository.save(loan);
    }

    @Transactional
    public LoanApplication disburseLoan(Long id) {
        LoanApplication loan = getLoanById(id);

        if (loan.getStatus() != LoanStatus.APPROVED) {
            throw new IllegalStateException(
                    "Cannot disburse loan in status: " + loan.getStatus() +
                    ". Loan must be APPROVED first.");
        }

        Client client = clientRepository.findById(loan.getClientId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Client not found for loan: " + id));

        if (client.getKycStatus() != KycStatus.VERIFIED) {
            throw new IllegalStateException(
                    "Cannot disburse — client KYC is not VERIFIED. Current status: " + client.getKycStatus());
        }

        loan.setStatus(LoanStatus.DISBURSED);
        return loanApplicationRepository.save(loan);
    }

    @Transactional
    public void deleteLoan(Long id) {
        LoanApplication loan = getLoanById(id);

        // Clean up EMI schedules and payments/collections
        List<EMISchedule> schedules = emiScheduleRepository.findByLoanId(id);
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
}
