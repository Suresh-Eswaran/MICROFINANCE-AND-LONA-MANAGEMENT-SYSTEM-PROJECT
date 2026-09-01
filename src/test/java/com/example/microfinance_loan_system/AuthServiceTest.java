package com.example.microfinance_loan_system;

import com.example.microfinance_loan_system.model.Role;
import com.example.microfinance_loan_system.model.User;
import com.example.microfinance_loan_system.repository.UserRepository;
import com.example.microfinance_loan_system.service.AuthService;
import com.example.microfinance_loan_system.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = User.builder()
                .id(1L)
                .email("test@microfin.com")
                .fullName("Test User")
                .password("encoded_old_password")
                .role(Role.LOAN_OFFICER)
                .build();
    }

    @Test
    void testForgotPassword_and_VerifyOtp_Success() {
        when(userRepository.findByEmail("test@microfin.com")).thenReturn(Optional.of(sampleUser));

        // 1. Request OTP
        String otp = authService.forgotPassword("test@microfin.com");
        assertNotNull(otp);
        assertEquals(6, otp.length());

        // 2. Verify OTP
        boolean isValid = authService.verifyOtp("test@microfin.com", otp);
        assertTrue(isValid);
    }

    @Test
    void testVerifyOtp_InvalidCode_ThrowsException() {
        when(userRepository.findByEmail("test@microfin.com")).thenReturn(Optional.of(sampleUser));

        String otp = authService.forgotPassword("test@microfin.com");

        assertThrows(IllegalArgumentException.class, () -> {
            authService.verifyOtp("test@microfin.com", "000000");
        });
    }

    @Test
    void testResetPassword_Success() {
        when(userRepository.findByEmail("test@microfin.com")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.encode("NewSecret123")).thenReturn("encoded_new_password");

        String otp = authService.forgotPassword("test@microfin.com");
        assertTrue(authService.verifyOtp("test@microfin.com", otp));

        authService.resetPassword("test@microfin.com", otp, "NewSecret123");

        verify(userRepository, atLeastOnce()).save(sampleUser);
    }
}
