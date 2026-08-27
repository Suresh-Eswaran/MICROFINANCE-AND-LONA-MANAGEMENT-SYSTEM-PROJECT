
package com.example.microfinance_loan_system.repository;

import com.example.microfinance_loan_system.model.EMISchedule;
import com.example.microfinance_loan_system.model.EmiStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EMIScheduleRepository extends JpaRepository<EMISchedule, Long> {
    List<EMISchedule> findByLoanId(Long loanId);
    List<EMISchedule> findByStatus(EmiStatus status);
    List<EMISchedule> findByDueDateBeforeAndStatusNot(LocalDate date, EmiStatus status);
    List<EMISchedule> findByLoanIdOrderByInstallmentNoAsc(Long loanId);
}
