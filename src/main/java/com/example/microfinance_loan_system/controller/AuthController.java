package com.example.microfinance_loan_system.controller;

import com.example.microfinance_loan_system.dto.ForgotPasswordRequest;
import com.example.microfinance_loan_system.dto.LoginRequest;
import com.example.microfinance_loan_system.dto.LoginResponse;
import com.example.microfinance_loan_system.dto.PasswordResetRequest;
import com.example.microfinance_loan_system.dto.RegisterRequest;
import com.example.microfinance_loan_system.dto.VerifyOtpRequest;
import com.example.microfinance_loan_system.model.User;
import com.example.microfinance_loan_system.service.AuthService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    @Autowired
    private AuthService authService;
    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest request) {
        User registered = authService.register(request);
        return new ResponseEntity<>(registered, HttpStatus.CREATED);
    }
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request.getEmail(), request.getPassword());
        return ResponseEntity.ok(response);
    }
    @PostMapping("/logout")
    public ResponseEntity<String> logout(
            @RequestHeader(value = "Authorization", required = false) String authHeader) {
        authService.logout(authHeader);
        return ResponseEntity.ok("Successfully logged out.");
    }
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(Map.of(
            "message", "A 6-digit verification code has been dispatched to " + request.getEmail() + ". Please check your inbox and spam folder."
        ));
    }
    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        authService.verifyOtp(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(Map.of(
            "message", "OTP verified successfully.",
            "valid", true,
            "email", request.getEmail(),
            "otp", request.getOtp()
        ));
    }
    @PostMapping("/verify-registration-otp")
    public ResponseEntity<Map<String, Object>> verifyRegistrationOtp(@Valid @RequestBody VerifyOtpRequest request) {
        User verifiedUser = authService.verifyRegistrationOtp(request.getEmail(), request.getOtp());
        return ResponseEntity.ok(Map.of(
            "message", "Email verified successfully! Your account is now active.",
            "valid", true,
            "email", verifiedUser.getEmail(),
            "status", verifiedUser.getStatus()
        ));
    }
    @PostMapping("/resend-registration-otp")
    public ResponseEntity<Map<String, String>> resendRegistrationOtp(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        authService.resendRegistrationOtp(email);
        return ResponseEntity.ok(Map.of(
            "message", "A new 6-digit verification code has been dispatched to " + email + ". Please check your inbox and spam folder."
        ));
    }
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@RequestBody PasswordResetRequest request) {
        authService.resetPassword(request.getEmail(), request.getOtp(), request.getNewPassword());
        return ResponseEntity.ok(Map.of(
            "message", "Password has been successfully reset. You can now login with your new password."
        ));
    }
}
