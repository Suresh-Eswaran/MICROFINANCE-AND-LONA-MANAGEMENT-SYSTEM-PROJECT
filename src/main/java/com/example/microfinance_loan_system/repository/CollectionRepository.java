

package com.example.microfinance_loan_system.repository;

import com.example.microfinance_loan_system.model.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {
    List<Collection> findAllByEmiId(Long emiId);
    List<Collection> findByCollectedBy(Long collectedBy);
    Optional<Collection> findByReceiptNumber(String receiptNumber);
}





