package com.example.microfinance_loan_system.service;

import com.example.microfinance_loan_system.dto.ProfileUpdateRequest;
import com.example.microfinance_loan_system.exception.ResourceNotFoundException;
import com.example.microfinance_loan_system.model.User;
import com.example.microfinance_loan_system.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public User updateUserProfile(Long id, ProfileUpdateRequest request) {
        User user = getUserById(id);
        
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        
        return userRepository.save(user);
    }

    public User approveUser(Long id) {
        User user = getUserById(id);
        user.setStatus("ACTIVE");
        return userRepository.save(user);
    }

    public User unlockUser(Long id) {
        User user = getUserById(id);
        user.setStatus("ACTIVE");
        return userRepository.save(user);
    }

    public User updateUserStatus(Long id, String status) {
        User user = getUserById(id);
        user.setStatus(status.toUpperCase());
        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = getUserById(id);
        userRepository.delete(user);
    }
}
