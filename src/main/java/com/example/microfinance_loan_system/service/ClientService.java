package com.example.microfinance_loan_system.service;
import com.example.microfinance_loan_system.dto.ClientRegistrationRequest;
import com.example.microfinance_loan_system.dto.ClientUpdateRequest;
import com.example.microfinance_loan_system.exception.DuplicateLoanException;
import com.example.microfinance_loan_system.exception.InvalidNameException;
import com.example.microfinance_loan_system.exception.InvalidPhoneException;
import com.example.microfinance_loan_system.exception.ResourceNotFoundException;
import com.example.microfinance_loan_system.model.Client;
import com.example.microfinance_loan_system.model.KycStatus;
import com.example.microfinance_loan_system.repository.ClientRepository;
import com.example.microfinance_loan_system.repository.LoanApplicationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
@Service
public class ClientService {

    @Autowired
    private ClientRepository clientRepository;
    @Autowired
    private LoanApplicationRepository loanApplicationRepository;
    public Client registerClient(ClientRegistrationRequest request) {
        validateName(request.getName());
        validatePhone(request.getPhoneNumber());
        if (clientRepository.findByPhoneNumber(request.getPhoneNumber()).isPresent()) {
            throw new DuplicateLoanException("Phone number already registered: " + request.getPhoneNumber());
        }

        Client client = Client.builder()
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .panNumber(request.getPanNumber())
                .cibilScore(request.getCibilScore())
                .groupId(request.getGroupId())
                .kycStatus(KycStatus.PENDING)
                .build();

        if (request.getPlainAadhaar() != null && !request.getPlainAadhaar().isBlank()) {
            client.setAadhaarHash(hashSHA256(request.getPlainAadhaar()));
        }

        return clientRepository.save(client);
    } 
    public Client getClientById(Long id) {
        return clientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Client not found with id: " + id));
    }
    public List<Client> getAllClients() {
        return clientRepository.findAll();
    }
    public Client updateClient(Long id, ClientUpdateRequest request) {
        Client client = getClientById(id);

        if (request.getName() != null) {
            validateName(request.getName());
            client.setName(request.getName());
        }
        if (request.getPhoneNumber() != null) {
            validatePhone(request.getPhoneNumber());
            // Make sure the new phone isn't already used by another client
            clientRepository.findByPhoneNumber(request.getPhoneNumber()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new DuplicateLoanException("Phone number already in use: " + request.getPhoneNumber());
                }
            });
            client.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getPanNumber() != null) {
            client.setPanNumber(request.getPanNumber());
        }
        if (request.getCibilScore() != null) {
            client.setCibilScore(request.getCibilScore());
        }
        if (request.getGroupId() != null) {
            client.setGroupId(request.getGroupId());
        }
        if (request.getKycStatus() != null) {
            try {
                client.setKycStatus(KycStatus.valueOf(request.getKycStatus().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid KYC status: " + request.getKycStatus());
            }
        }

        return clientRepository.save(client);
    }
    @Transactional
    public void deleteClient(Long id) {
        Client client = getClientById(id);
        List<?> loans = loanApplicationRepository.findByClientId(id);
        if (!loans.isEmpty()) {
            throw new IllegalStateException(
                    "Cannot delete client " + id + " — they have " + loans.size() + " existing loan application(s)."
            );
        }
        clientRepository.delete(client);
    } 
    private void validateName(String name) {
        if (name == null || !name.matches("[a-zA-Z ]{2,100}")) {
            throw new InvalidNameException("Name must be 2–100 alphabetic characters: " + name);
        }
    }
    private void validatePhone(String phone) {
        if (phone == null || !phone.matches("\\d{10}")) {
            throw new InvalidPhoneException("Phone number must be exactly 10 digits: " + phone);
        }
    }
    private String hashSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}
