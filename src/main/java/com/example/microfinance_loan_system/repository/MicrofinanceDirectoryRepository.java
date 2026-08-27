package com.example.microfinance_loan_system.repository;

import com.example.microfinance_loan_system.model.MicrofinanceDirectory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MicrofinanceDirectoryRepository extends JpaRepository<MicrofinanceDirectory, Long> {
    @Query("SELECT md FROM MicrofinanceDirectory md WHERE md.employeeOrUserId = :employeeOrUserId")
    Optional<MicrofinanceDirectory> findByEmployeeOrUserId(@Param("employeeOrUserId") String employeeOrUserId);
}
