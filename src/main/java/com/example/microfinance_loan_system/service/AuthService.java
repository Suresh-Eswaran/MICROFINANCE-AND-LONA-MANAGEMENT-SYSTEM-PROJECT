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
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private static class OtpRecord {
        final String otp;
        final Instant expiresAt;

        OtpRecord(String otp, Instant expiresAt) {
            this.otp = otp;
            this.expiresAt = expiresAt;
        }
    }

    private final Map<String, OtpRecord> otpStore = new ConcurrentHashMap<>();
    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private TokenBlacklistService tokenBlacklistService;
    @Autowired
    private EmailService emailService;
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

    public String forgotPassword(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        String normalizedEmail = email.trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("No account found with email: " + email));

        // Generate 6-digit OTP
        int code = 100000 + secureRandom.nextInt(900000);
        String otp = String.valueOf(code);
        Instant expiresAt = Instant.now().plusSeconds(15 * 60); // 15 minutes validity

        otpStore.put(normalizedEmail, new OtpRecord(otp, expiresAt));
        logger.info("Generated Password Reset OTP for [{}]: {}", normalizedEmail, otp);

        // Send OTP via email
        try {
            emailService.sendOtpEmail(normalizedEmail, otp);
        } catch (Exception e) {
            logger.error("Could not send email to [{}]: {}", normalizedEmail, e.getMessage());
        }

        return otp;
    }

    public boolean verifyOtp(String email, String otp) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (otp == null || otp.trim().isEmpty()) {
            throw new IllegalArgumentException("OTP code is required.");
        }
        String normalizedEmail = email.trim().toLowerCase();
        OtpRecord record = otpStore.get(normalizedEmail);

        if (record == null) {
            throw new IllegalArgumentException("No OTP requested or OTP has expired. Please request a new OTP.");
        }

        if (Instant.now().isAfter(record.expiresAt)) {
            otpStore.remove(normalizedEmail);
            throw new IllegalArgumentException("OTP has expired. Please request a new one.");
        }

        if (!record.otp.equals(otp.trim())) {
            throw new IllegalArgumentException("Invalid OTP code. Please check and try again.");
        }

        return true;
    }

    public void resetPassword(String email, String otp, String newPassword) {
        if (email == null || otp == null || newPassword == null) {
            throw new IllegalArgumentException("Email, OTP, and new password are required.");
        }
        String normalizedEmail = email.trim().toLowerCase();
        OtpRecord record = otpStore.get(normalizedEmail);

        if (record == null) {
            throw new IllegalArgumentException("No OTP requested or OTP has expired. Please request a new OTP.");
        }

        if (Instant.now().isAfter(record.expiresAt)) {
            otpStore.remove(normalizedEmail);
            throw new IllegalArgumentException("OTP has expired. Please request a new one.");
        }

        if (!record.otp.equals(otp.trim())) {
            throw new IllegalArgumentException("Invalid OTP code. Please check and try again.");
        }

        if (newPassword.trim().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        user.setPassword(passwordEncoder.encode(newPassword.trim()));
        userRepository.save(user);

        // Invalidate OTP after successful reset
        otpStore.remove(normalizedEmail);
        logger.info("Password successfully reset for user [{}]", normalizedEmail);
    }

    public User getProfile(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
