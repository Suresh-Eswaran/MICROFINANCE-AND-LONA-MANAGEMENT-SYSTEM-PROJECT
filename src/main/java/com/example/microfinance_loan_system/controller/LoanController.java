
package com.example.microfinance_loan_system.controller;

import com.example.microfinance_loan_system.dto.LoanApplyRequest;
import com.example.microfinance_loan_system.model.LoanApplication;
import com.example.microfinance_loan_system.service.LoanService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/loans")
public class LoanController {
    @Autowired
    private LoanService loanService;
    @PostMapping("/apply")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','CREDIT_OFFICER','LOAN_OFFICER')")
    public ResponseEntity<LoanApplication> applyForLoan(@Valid @RequestBody LoanApplyRequest request) {
        LoanApplication loan = loanService.applyForLoan(request);
        return new ResponseEntity<>(loan, HttpStatus.CREATED);
    }
    @GetMapping("/{id}")
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
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','CREDIT_OFFICER','LOAN_OFFICER','COLLECTIONS_AGENT')")
    public ResponseEntity<List<LoanApplication>> getAllLoans() {
        return ResponseEntity.ok(loanService.getAllLoans());
    }
    @PutMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<LoanApplication> approveLoan(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.approveLoan(id));
    }
    @PostMapping("/{id}/disburse")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<LoanApplication> disburseLoan(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.disburseLoan(id));
    }
}
