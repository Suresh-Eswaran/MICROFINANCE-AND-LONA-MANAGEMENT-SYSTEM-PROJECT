package com.example.microfinance_loan_system.controller;

import com.example.microfinance_loan_system.dto.LoginRequest;
import com.example.microfinance_loan_system.dto.LoginResponse;
import com.example.microfinance_loan_system.dto.RegisterRequest;
import com.example.microfinance_loan_system.model.User;
import com.example.microfinance_loan_system.service.AuthService;
import jakarta.validation.Valid;
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
}
