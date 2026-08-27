
package com.example.microfinance_loan_system.repository;
import com.example.microfinance_loan_system.model.LoanApplication;
import com.example.microfinance_loan_system.model.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanApplicationRepository extends JpaRepository<LoanApplication, Long> {
    List<LoanApplication> findByClientId(Long clientId);
    List<LoanApplication> findByOfficerId(Long officerId);
    List<LoanApplication> findByStatus(LoanStatus status);
    int countByOfficerIdAndStatusIn(Long officerId, List<LoanStatus> statuses);
    long countByStatusIn(List<LoanStatus> statuses);
}
