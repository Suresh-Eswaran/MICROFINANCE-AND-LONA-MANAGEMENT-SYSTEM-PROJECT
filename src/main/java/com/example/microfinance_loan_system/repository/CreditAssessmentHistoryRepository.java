package com.example.microfinance_loan_system.repository;

import com.example.microfinance_loan_system.model.CreditAssessmentHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CreditAssessmentHistoryRepository extends JpaRepository<CreditAssessmentHistory, Long> {
    List<CreditAssessmentHistory> findByClientId(Long clientId);
    List<CreditAssessmentHistory> findByClientIdOrderByAssessmentDateDesc(Long clientId);
}
