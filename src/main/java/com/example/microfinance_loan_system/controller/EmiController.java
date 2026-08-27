package com.example.microfinance_loan_system.controller;

import com.example.microfinance_loan_system.dto.EmiStatusUpdateRequest;
import com.example.microfinance_loan_system.model.EMISchedule;
import com.example.microfinance_loan_system.service.EmiService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/emi")
public class EmiController {

    @Autowired
    private EmiService emiService;
 
    @PostMapping("/generate/{loanId}")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','CREDIT_OFFICER','LOAN_OFFICER')")
    public ResponseEntity<List<EMISchedule>> generateSchedule(@PathVariable Long loanId) {
        List<EMISchedule> schedule = emiService.generateSchedule(loanId);
        return new ResponseEntity<>(schedule, HttpStatus.CREATED);
    } 
    @GetMapping("/schedule/{loanId}")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','CREDIT_OFFICER','LOAN_OFFICER','COLLECTIONS_AGENT','CLIENT')")
    public ResponseEntity<List<EMISchedule>> getSchedule(@PathVariable Long loanId) {
        return ResponseEntity.ok(emiService.getScheduleByLoanId(loanId));
    } 
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','CREDIT_OFFICER','LOAN_OFFICER','COLLECTIONS_AGENT')")
    public ResponseEntity<EMISchedule> updateStatus(@PathVariable Long id,
                                                    @Valid @RequestBody EmiStatusUpdateRequest request) {
        return ResponseEntity.ok(emiService.updateEmiStatus(id, request.getStatus()));
    }
}
