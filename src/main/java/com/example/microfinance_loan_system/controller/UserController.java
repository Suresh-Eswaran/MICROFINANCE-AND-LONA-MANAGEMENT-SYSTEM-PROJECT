package com.example.microfinance_loan_system.controller;

import com.example.microfinance_loan_system.dto.ProfileUpdateRequest;
import com.example.microfinance_loan_system.model.User;
import com.example.microfinance_loan_system.service.AuthService;
import com.example.microfinance_loan_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER','CREDIT_OFFICER','LOAN_OFFICER','COLLECTIONS_AGENT','CLIENT')")
    public ResponseEntity<User> getProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        // Uses UserService to fetch profile instead of AuthService
        return ResponseEntity.ok(userService.getUserByEmail(email));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','BRANCH_MANAGER')")
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<User> updateUser(@PathVariable Long id, @RequestBody ProfileUpdateRequest request) {
        return ResponseEntity.ok(userService.updateUserProfile(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}
