package com.example.microfinance_loan_system.controller;

import com.example.microfinance_loan_system.dto.CollectionRequest;
import com.example.microfinance_loan_system.model.Collection;
import com.example.microfinance_loan_system.model.EMISchedule;
import com.example.microfinance_loan_system.model.EmiStatus;
import com.example.microfinance_loan_system.repository.CollectionRepository;
import com.example.microfinance_loan_system.repository.EMIScheduleRepository;
import com.example.microfinance_loan_system.service.CollectionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/collections")
public class CollectionController {

    @Autowired
    private CollectionService collectionService;

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private EMIScheduleRepository emiScheduleRepository;

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

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','LOAN_OFFICER','COLLECTIONS_AGENT')")
    public ResponseEntity<List<Collection>> getAllCollections() {
        return ResponseEntity.ok(collectionRepository.findAllByOrderByCollectionDateDesc());
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        long paidCount    = emiScheduleRepository.findByStatus(EmiStatus.PAID).size();
        long overdueCount = emiScheduleRepository.findByStatus(EmiStatus.OVERDUE).size();
        long total        = paidCount + overdueCount;
        double rate       = total == 0 ? 100.0 : ((double) paidCount / total) * 100.0;

        BigDecimal totalCollected = collectionRepository.sumTotalCollected();

        BigDecimal todayTarget = emiScheduleRepository
                .findByDueDateBeforeAndStatusNot(LocalDate.now().plusDays(1), EmiStatus.PAID)
                .stream()
                .filter(e -> e.getStatus() == EmiStatus.PENDING || e.getStatus() == EmiStatus.OVERDUE)
                .map(e -> e.getEmiAmount() != null ? e.getEmiAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> map = new HashMap<>();
        map.put("collectionRate", Math.round(rate * 10.0) / 10.0);
        map.put("totalCollected", totalCollected);
        map.put("todayTarget",    todayTarget);
        return ResponseEntity.ok(map);
    }
}
