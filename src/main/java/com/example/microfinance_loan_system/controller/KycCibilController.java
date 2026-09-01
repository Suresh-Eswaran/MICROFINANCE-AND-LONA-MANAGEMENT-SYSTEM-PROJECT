package com.example.microfinance_loan_system.controller;

import com.example.microfinance_loan_system.dto.CibilRequest;
import com.example.microfinance_loan_system.dto.KycRequest;
import com.example.microfinance_loan_system.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class KycCibilController {

    @Autowired
    private ClientService clientService;

    @PostMapping("/kyc/verify")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','CREDIT_OFFICER','LOAN_OFFICER','CLIENT')")
    public ResponseEntity<Map<String, Object>> verifyKyc(@RequestBody KycRequest request) {
        return ResponseEntity.ok(clientService.verifyKyc(request));
    }

    @PostMapping("/cibil/enquiry")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','CREDIT_OFFICER','LOAN_OFFICER','CLIENT')")
    public ResponseEntity<Map<String, Object>> cibilEnquiry(@RequestBody CibilRequest request) {
        return ResponseEntity.ok(clientService.cibilEnquiry(request));
    }
}
