
package com.example.microfinance_loan_system.controller;

import com.example.microfinance_loan_system.dto.LoanApplyRequest;
import com.example.microfinance_loan_system.model.LoanApplication;
import com.example.microfinance_loan_system.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loans")
public class LoanController {
    @Autowired
    private LoanService loanService;

    @GetMapping("/my-loans")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','CREDIT_OFFICER','LOAN_OFFICER','COLLECTIONS_AGENT','CLIENT')")
    public ResponseEntity<List<LoanApplication>> getMyLoans() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(loanService.getLoansForCurrentUser(email));
    }

    @GetMapping("/{id}/statement")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','CREDIT_OFFICER','LOAN_OFFICER','COLLECTIONS_AGENT','CLIENT')")
    public ResponseEntity<Map<String, Object>> getLoanStatement(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getLoanStatement(id));
    }

    @PostMapping("/apply")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','CREDIT_OFFICER','LOAN_OFFICER','CLIENT')")
    public ResponseEntity<LoanApplication> applyForLoan(@Valid @RequestBody LoanApplyRequest request) {
        LoanApplication loan = loanService.applyForLoan(request);
        return new ResponseEntity<>(loan, HttpStatus.CREATED);
    }
    @GetMapping("/{id:[0-9]+}")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','CREDIT_OFFICER','LOAN_OFFICER','COLLECTIONS_AGENT','CLIENT')")
    public ResponseEntity<LoanApplication> getLoan(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getLoanById(id));
    } 
    @GetMapping("/client/{clientId}")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','CREDIT_OFFICER','LOAN_OFFICER','COLLECTIONS_AGENT','CLIENT')")
    public ResponseEntity<List<LoanApplication>> getClientLoans(@PathVariable Long clientId) {
        return ResponseEntity.ok(loanService.getLoansByClientId(clientId));
    }
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','CREDIT_OFFICER','LOAN_OFFICER','COLLECTIONS_AGENT','CLIENT')")
    public ResponseEntity<List<LoanApplication>> getAllLoans() {
        return ResponseEntity.ok(loanService.getAllLoans());
    }
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<LoanApplication> approveLoan(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.approveLoan(id));
    }
    @PutMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<LoanApplication> rejectLoan(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.rejectLoan(id));
    }
    @PostMapping("/{id}/disburse")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<LoanApplication> disburseLoan(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.disburseLoan(id));
    }
}
