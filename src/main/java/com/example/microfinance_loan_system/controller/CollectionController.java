package com.example.microfinance_loan_system.controller;

import com.example.microfinance_loan_system.dto.CollectionRequest;
import com.example.microfinance_loan_system.model.Collection;
import com.example.microfinance_loan_system.model.EMISchedule;
import com.example.microfinance_loan_system.service.CollectionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/api/collections")
public class CollectionController {

    @Autowired
    private CollectionService collectionService;
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','LOAN_OFFICER','COLLECTIONS_AGENT')")
    public ResponseEntity<Collection> recordCollection(@Valid @RequestBody CollectionRequest request) {
        Collection collection = collectionService.recordCollection(request);
        return new ResponseEntity<>(collection, HttpStatus.CREATED);
    } 
    @GetMapping("/overdue")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','LOAN_OFFICER','COLLECTIONS_AGENT')")
    public ResponseEntity<List<EMISchedule>> getOverdueCollections() {
        return ResponseEntity.ok(collectionService.getOverdueEmis());
    } 
    @GetMapping("/emi/{emiId}")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','CREDIT_OFFICER','LOAN_OFFICER','COLLECTIONS_AGENT','CLIENT')")
    public ResponseEntity<List<Collection>> getCollectionsByEmi(@PathVariable Long emiId) {
        return ResponseEntity.ok(collectionService.getCollectionsByEmiId(emiId));
    }
}
