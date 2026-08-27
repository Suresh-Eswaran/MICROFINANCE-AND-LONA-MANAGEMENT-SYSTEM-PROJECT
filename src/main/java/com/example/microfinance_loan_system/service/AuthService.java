package com.example.microfinance_loan_system.service;

import com.example.microfinance_loan_system.config.JwtUtils;
import com.example.microfinance_loan_system.config.TokenBlacklistService;
import com.example.microfinance_loan_system.dto.LoginResponse;
import com.example.microfinance_loan_system.dto.RegisterRequest;
import com.example.microfinance_loan_system.exception.DuplicateLoanException;
import com.example.microfinance_loan_system.exception.ResourceNotFoundException;
import com.example.microfinance_loan_system.model.Role;
import com.example.microfinance_loan_system.model.User;
import com.example.microfinance_loan_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
@Service
public class AuthService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private TokenBlacklistService tokenBlacklistService;
    public User register(RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new DuplicateLoanException("Email is already registered: " + request.getEmail());
        }
        Role role;
        try {
            role = Role.valueOf(request.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid role: " + request.getRole()
                    + ". Valid roles: ADMIN, BRANCH_MANAGER, CREDIT_OFFICER, LOAN_OFFICER, COLLECTIONS_AGENT, CLIENT");
        }
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .branch(request.getBranch())
                .build();

        return userRepository.save(user);
    } 
    public LoginResponse login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with email: " + email));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials.");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getEmail(), user.getRole());
        return new LoginResponse(token, user.getId(), user.getEmail(), user.getRole().name());
    } 
    public void logout(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklistService.blacklistToken(token);
        }
    }

    public User getProfile(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
