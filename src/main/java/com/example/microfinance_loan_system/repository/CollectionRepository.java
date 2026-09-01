package com.example.microfinance_loan_system.repository;

import com.example.microfinance_loan_system.model.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {
    List<Collection> findAllByEmiId(Long emiId);
    List<Collection> findByCollectedBy(Long collectedBy);
    Optional<Collection> findByReceiptNumber(String receiptNumber);
    List<Collection> findAllByOrderByCollectionDateDesc();
    List<Collection> findByCollectionDateAfter(LocalDateTime since);

    @Query("SELECT COALESCE(SUM(c.amountCollected), 0) FROM Collection c")
    BigDecimal sumTotalCollected();

    @Query("SELECT COALESCE(SUM(c.amountCollected), 0) FROM Collection c WHERE c.collectionDate >= :since")
    BigDecimal sumCollectedSince(LocalDateTime since);
}





