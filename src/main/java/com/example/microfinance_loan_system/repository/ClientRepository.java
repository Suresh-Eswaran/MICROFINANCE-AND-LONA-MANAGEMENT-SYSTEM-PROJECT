

package com.example.microfinance_loan_system.repository;

import com.example.microfinance_loan_system.model.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client,Long> {
    Optional<Client> findByPhoneNumber(String phoneNumber);
    List<Client> findByGroupId(Long groupId);
    List<Client> findByKycStatusNot(com.example.microfinance_loan_system.model.KycStatus kycStatus);
}
