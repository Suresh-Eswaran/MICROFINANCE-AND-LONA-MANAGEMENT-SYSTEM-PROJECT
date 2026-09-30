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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

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

    @Autowired
    private AuditLogService auditLogService;

    public User register(RegisterRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        Optional<User> existingUserOpt = userRepository.findByEmail(normalizedEmail);
        if (existingUserOpt.isPresent()) {
            User existing = existingUserOpt.get();
            if (!"PENDING_VERIFICATION".equalsIgnoreCase(existing.getStatus())) {
                auditLogService.log("USER_REGISTRATION_FAILED", existing.getId(), normalizedEmail, "Email already registered: " + normalizedEmail, false);
                throw new DuplicateLoanException("Email is already registered: " + request.getEmail());
            }
            Role role;
            try {
                role = Role.valueOf(request.getRole().toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid role: " + request.getRole()
                        + ". Valid roles: ADMIN, BRANCH_MANAGER, CREDIT_OFFICER, LOAN_OFFICER, COLLECTIONS_AGENT, CLIENT");
            }
            existing.setFullName(request.getFullName());
            existing.setPassword(passwordEncoder.encode(request.getPassword()));
            existing.setRole(role);
            existing.setBranch(request.getBranch());
            User savedUser = userRepository.save(existing);
            sendRegistrationOtp(savedUser);
            return savedUser;
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
                .email(normalizedEmail)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .branch(request.getBranch())
                .status("PENDING_VERIFICATION")
                .build();

        User savedUser = userRepository.save(user);
        auditLogService.log("USER_REGISTERED", savedUser.getId(), savedUser.getEmail(), "Role: " + savedUser.getRole() + ", branch: " + savedUser.getBranch() + " (Pending OTP Verification)", true);

        sendRegistrationOtp(savedUser);
        return savedUser;
    }

    public String sendRegistrationOtp(User user) {
        String normalizedEmail = user.getEmail().trim().toLowerCase();
        int code = 100000 + secureRandom.nextInt(900000);
        String otp = String.valueOf(code);
        Instant expiresAt = Instant.now().plus(15, ChronoUnit.MINUTES);

        otpStore.put(normalizedEmail, new OtpRecord(otp, expiresAt));
        logger.info("Generated Registration OTP for [{}]: {}", normalizedEmail, otp);
        auditLogService.log("REGISTRATION_OTP_REQUESTED", user.getId(), normalizedEmail, "Registration verification OTP generated.", true);

        CompletableFuture.runAsync(() -> {
            try {
                emailService.sendRegistrationOtpEmail(normalizedEmail, otp, user.getFullName());
            } catch (Exception e) {
                logger.error("Could not send registration email to [{}]: {}", normalizedEmail, e.getMessage());
            }
        });

        return otp;
    }

    public String resendRegistrationOtp(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        String normalizedEmail = email.trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("No registered account found with email: " + email));

        if (!"PENDING_VERIFICATION".equalsIgnoreCase(user.getStatus())) {
            throw new IllegalArgumentException("Account is already verified or active.");
        }

        return sendRegistrationOtp(user);
    }

    public User verifyRegistrationOtp(String email, String otp) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (otp == null || otp.trim().isEmpty()) {
            throw new IllegalArgumentException("OTP code is required.");
        }
        String normalizedEmail = email.trim().toLowerCase();
        OtpRecord record = otpStore.get(normalizedEmail);

        if (record == null) {
            auditLogService.log("OTP_VERIFY_FAILED", null, normalizedEmail, "No active OTP or OTP expired.", false);
            throw new IllegalArgumentException("No OTP requested or OTP has expired. Please click Resend OTP.");
        }

        if (Instant.now().isAfter(record.expiresAt)) {
            otpStore.remove(normalizedEmail);
            auditLogService.log("OTP_VERIFY_FAILED", null, normalizedEmail, "OTP expired.", false);
            throw new IllegalArgumentException("OTP has expired. Please click Resend OTP.");
        }

        if (!record.otp.equals(otp.trim())) {
            auditLogService.log("OTP_VERIFY_FAILED", null, normalizedEmail, "Invalid OTP code entered.", false);
            throw new IllegalArgumentException("Invalid 6-digit OTP code. Please check and try again.");
        }

        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        user.setStatus("ACTIVE");
        User activatedUser = userRepository.save(user);

        otpStore.remove(normalizedEmail);
        logger.info("Account verified and activated for user [{}]", normalizedEmail);
        auditLogService.log("USER_VERIFIED", activatedUser.getId(), activatedUser.getEmail(), "Account email verified via OTP and activated.", true);
        return activatedUser;
    }

    public LoginResponse login(String email, String password) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            auditLogService.log("LOGIN_FAILED", null, email, "Account does not exist with email: " + email, false);
            throw new ResourceNotFoundException("No account found with email: " + email);
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(password, user.getPassword())) {
            auditLogService.log("LOGIN_FAILED", user.getId(), user.getEmail(), "Invalid credentials / password mismatch", false);
            throw new IllegalArgumentException("Invalid credentials.");
        }

        if ("PENDING_VERIFICATION".equalsIgnoreCase(user.getStatus())) {
            auditLogService.log("LOGIN_FAILED", user.getId(), user.getEmail(), "Attempted login with unverified email.", false);
            throw new IllegalArgumentException("Your email is not verified yet. Please enter the OTP sent to your email to activate your account.");
        }

        String token = jwtUtils.generateToken(user.getId(), user.getEmail(), user.getRole());
        auditLogService.log("USER_LOGIN", user.getId(), user.getEmail(), "Successful authentication via credentials.", true);
        return new LoginResponse(token, user.getId(), user.getEmail(), user.getRole().name());
    }

    public void logout(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenBlacklistService.blacklistToken(token);
            try {
                String email = jwtUtils.getEmailFromToken(token);
                auditLogService.log("USER_LOGOUT", null, email, "User logged out successfully.", true);
            } catch (Exception ignored) {
            }
        }
    }

    public String forgotPassword(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        String normalizedEmail = email.trim().toLowerCase();
        User user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> {
                    auditLogService.log("PASSWORD_RESET_FAILED", null, normalizedEmail, "Account not found for password reset.", false);
                    return new ResourceNotFoundException("No account found with email: " + email);
                });

        // Generate 6-digit OTP
        int code = 100000 + secureRandom.nextInt(900000);
        String otp = String.valueOf(code);
        Instant expiresAt = Instant.now().plusSeconds(15 * 60); // 15 minutes validity

        otpStore.put(normalizedEmail, new OtpRecord(otp, expiresAt));
        logger.info("Generated Password Reset OTP for [{}]: {}", normalizedEmail, otp);

        auditLogService.log("PASSWORD_RESET_REQUESTED", user.getId(), user.getEmail(), "Password reset OTP requested.", true);

        // Send OTP via email asynchronously
        java.util.concurrent.CompletableFuture.runAsync(() -> {
            try {
                emailService.sendOtpEmail(normalizedEmail, otp);
            } catch (Exception e) {
                logger.error("Could not send email to [{}]: {}", normalizedEmail, e.getMessage());
            }
        });

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
            auditLogService.log("OTP_VERIFY_FAILED", null, normalizedEmail, "No active OTP or OTP expired.", false);
            throw new IllegalArgumentException("No OTP requested or OTP has expired. Please request a new OTP.");
        }

        if (Instant.now().isAfter(record.expiresAt)) {
            otpStore.remove(normalizedEmail);
            auditLogService.log("OTP_VERIFY_FAILED", null, normalizedEmail, "OTP expired.", false);
            throw new IllegalArgumentException("OTP has expired. Please request a new one.");
        }

        if (!record.otp.equals(otp.trim())) {
            auditLogService.log("OTP_VERIFY_FAILED", null, normalizedEmail, "Invalid OTP code entered.", false);
            throw new IllegalArgumentException("Invalid OTP code. Please check and try again.");
        }

        auditLogService.log("OTP_VERIFIED", null, normalizedEmail, "Security OTP verified successfully.", true);

        // If user account is pending verification, activate it
        userRepository.findByEmail(normalizedEmail).ifPresent(u -> {
            if ("PENDING_VERIFICATION".equalsIgnoreCase(u.getStatus())) {
                u.setStatus("ACTIVE");
                userRepository.save(u);
                auditLogService.log("USER_VERIFIED", u.getId(), u.getEmail(), "Account email verified via OTP and activated.", true);
            }
        });

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
            auditLogService.log("PASSWORD_RESET_FAILED", null, normalizedEmail, "Invalid OTP code provided during reset.", false);
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
        auditLogService.log("PASSWORD_RESET_SUCCESS", user.getId(), user.getEmail(), "Password successfully reset.", true);
    }

    public User getProfile(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }
}
