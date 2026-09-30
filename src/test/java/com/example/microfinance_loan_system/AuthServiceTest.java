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

    @Mock
    private com.example.microfinance_loan_system.service.AuditLogService auditLogService;

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

    @Test
    void testRegister_SetsPendingVerification_And_SendsOtp() {
        com.example.microfinance_loan_system.dto.RegisterRequest regReq = new com.example.microfinance_loan_system.dto.RegisterRequest();
        regReq.setFullName("New Member");
        regReq.setEmail("newmember@microfin.com");
        regReq.setPassword("Password123");
        regReq.setRole("CLIENT");
        regReq.setBranch("Main Branch");

        when(userRepository.findByEmail("newmember@microfin.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password123")).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User registered = authService.register(regReq);
        assertNotNull(registered);
        assertEquals("PENDING_VERIFICATION", registered.getStatus());
        assertEquals("newmember@microfin.com", registered.getEmail());

        verify(emailService, atLeastOnce()).sendRegistrationOtpEmail(eq("newmember@microfin.com"), anyString(), eq("New Member"));
    }

    @Test
    void testVerifyRegistrationOtp_Success() {
        User pendingUser = User.builder()
                .id(2L)
                .email("pending@microfin.com")
                .fullName("Pending User")
                .status("PENDING_VERIFICATION")
                .role(Role.CLIENT)
                .build();

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String otp = authService.sendRegistrationOtp(pendingUser);
        assertNotNull(otp);
        assertEquals(6, otp.length());

        when(userRepository.findByEmail("pending@microfin.com")).thenReturn(Optional.of(pendingUser));
        User activated = authService.verifyRegistrationOtp("pending@microfin.com", otp);

        assertEquals("ACTIVE", activated.getStatus());
    }

    @Test
    void testLogin_PendingVerificationUser_ThrowsException() {
        User unverifiedUser = User.builder()
                .id(3L)
                .email("unverified@microfin.com")
                .password("encoded_pass")
                .status("PENDING_VERIFICATION")
                .role(Role.CLIENT)
                .build();

        when(userRepository.findByEmail("unverified@microfin.com")).thenReturn(Optional.of(unverifiedUser));
        when(passwordEncoder.matches("raw_pass", "encoded_pass")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            authService.login("unverified@microfin.com", "raw_pass");
        });

        assertTrue(ex.getMessage().contains("Your email is not verified yet"));
    }
}
