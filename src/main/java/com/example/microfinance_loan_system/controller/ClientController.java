package com.example.microfinance_loan_system.controller;

import com.example.microfinance_loan_system.dto.ClientRegistrationRequest;
import com.example.microfinance_loan_system.dto.ClientUpdateRequest;
import com.example.microfinance_loan_system.dto.IncomeVerificationRequest;
import com.example.microfinance_loan_system.dto.ScoreOverrideRequest;
import com.example.microfinance_loan_system.model.Client;
import com.example.microfinance_loan_system.service.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','CREDIT_OFFICER','LOAN_OFFICER','CLIENT')")
    public ResponseEntity<Client> registerClient(@Valid @RequestBody ClientRegistrationRequest request) {
        Client client = clientService.registerClient(request);
        return new ResponseEntity<>(client, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','CREDIT_OFFICER','LOAN_OFFICER','COLLECTIONS_AGENT','CLIENT')")
    public ResponseEntity<Client> getClient(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getClientById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','CREDIT_OFFICER','LOAN_OFFICER','COLLECTIONS_AGENT','CLIENT')")
    public ResponseEntity<List<Client>> getAllClients() {
        return ResponseEntity.ok(clientService.getAllClients());
    } 

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','CREDIT_OFFICER','LOAN_OFFICER','CLIENT')")
    public ResponseEntity<Client> updateClient(@PathVariable Long id,
                                               @Valid @RequestBody ClientUpdateRequest request) {
        return ResponseEntity.ok(clientService.updateClient(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<Map<String, String>> deleteClient(@PathVariable Long id) {
        clientService.deleteClient(id);
        return ResponseEntity.ok(Map.of("message", "Client #" + id + " and completed loan records deleted successfully."));
    }

    @PostMapping("/{id}/assess-credit")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','CREDIT_OFFICER','LOAN_OFFICER','COLLECTIONS_AGENT','CLIENT')")
    public ResponseEntity<Client> assessCredit(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.assessCredit(id));
    }

    @GetMapping("/{id}/credit-report")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','CREDIT_OFFICER','LOAN_OFFICER','COLLECTIONS_AGENT','CLIENT')")
    public ResponseEntity<Map<String, String>> getCreditReport(@PathVariable Long id) {
        return ResponseEntity.ok(clientService.getCreditReport(id));
    }

    @PostMapping("/{id}/verify-income")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','CREDIT_OFFICER','LOAN_OFFICER','CLIENT')")
    public ResponseEntity<Client> verifyIncome(@PathVariable Long id,
                                               @RequestBody IncomeVerificationRequest request) {
        return ResponseEntity.ok(clientService.verifyIncome(id, request));
    }

    @PutMapping("/{id}/override-score")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','CREDIT_OFFICER')")
    public ResponseEntity<Client> overrideScore(@PathVariable Long id,
                                                @RequestBody ScoreOverrideRequest request) {
        return ResponseEntity.ok(clientService.overrideScore(id, request));
    }
}
