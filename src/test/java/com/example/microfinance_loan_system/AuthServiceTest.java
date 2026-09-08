package com.example.microfinance_loan_system;

import com.example.microfinance_loan_system.config.JwtUtils;
import com.example.microfinance_loan_system.dto.RegisterRequest;
import com.example.microfinance_loan_system.model.Role;
import com.example.microfinance_loan_system.model.User;
import com.example.microfinance_loan_system.repository.UserRepository;
import com.example.microfinance_loan_system.service.AuditLogService;
import com.example.microfinance_loan_system.service.AuthService;
import com.example.microfinance_loan_system.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
    private AuditLogService auditLogService;

    @Mock
    private JwtUtils jwtUtils;

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
                .status("ACTIVE")
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
    void testRegister_NewUser_GeneratesOtpAndStatusPendingVerification() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("newuser@microfin.com");
        req.setFullName("New User");
        req.setPassword("secretPass123");
        req.setRole("LOAN_OFFICER");
        req.setBranch("Head Office");

        when(userRepository.findByEmail("newuser@microfin.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secretPass123")).thenReturn("encodedSecret");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(99L);
            return u;
        });

        Map<String, Object> result = authService.register(req);

        assertNotNull(result);
        assertEquals("PENDING_VERIFICATION", result.get("status"));
        assertTrue((Boolean) result.get("requiresOtp"));
        assertNotNull(result.get("otp"));
        assertEquals(6, ((String) result.get("otp")).length());

        verify(emailService, times(1)).sendRegistrationOtpEmail(eq("newuser@microfin.com"), eq("New User"), anyString());
    }

    @Test
    void testVerifyEmailOtp_Success_TransitionsToActive() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("verify@microfin.com");
        req.setFullName("Verify User");
        req.setPassword("secretPass123");
        req.setRole("LOAN_OFFICER");
        req.setBranch("Head Office");

        User pendingUser = User.builder()
                .id(100L)
                .email("verify@microfin.com")
                .fullName("Verify User")
                .password("encodedSecret")
                .role(Role.LOAN_OFFICER)
                .status("PENDING_VERIFICATION")
                .build();

        when(userRepository.findByEmail("verify@microfin.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("secretPass123")).thenReturn("encodedSecret");
        when(userRepository.save(any(User.class))).thenReturn(pendingUser);

        Map<String, Object> regResult = authService.register(req);
        String otp = (String) regResult.get("otp");

        when(userRepository.findByEmail("verify@microfin.com")).thenReturn(Optional.of(pendingUser));

        Map<String, Object> verifyResult = authService.verifyEmailOtp("verify@microfin.com", otp);

        assertEquals("ACTIVE", verifyResult.get("status"));
        assertTrue((Boolean) verifyResult.get("verified"));
        verify(userRepository, atLeastOnce()).save(pendingUser);
    }

    @Test
    void testVerifyEmailOtp_InvalidOtp_ThrowsException() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("invalidotp@microfin.com");
        req.setFullName("Invalid OTP User");
        req.setPassword("secretPass123");
        req.setRole("CLIENT");
        req.setBranch("Head Office");

        User pendingUser = User.builder()
                .id(101L)
                .email("invalidotp@microfin.com")
                .fullName("Invalid OTP User")
                .password("encodedSecret")
                .role(Role.CLIENT)
                .status("PENDING_VERIFICATION")
                .build();

        when(userRepository.findByEmail("invalidotp@microfin.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode(any())).thenReturn("encodedSecret");
        when(userRepository.save(any(User.class))).thenReturn(pendingUser);

        authService.register(req);

        assertThrows(IllegalArgumentException.class, () -> {
            authService.verifyEmailOtp("invalidotp@microfin.com", "999999");
        });
    }

    @Test
    void testLogin_PendingVerification_ThrowsException() {
        User unverifiedUser = User.builder()
                .id(102L)
                .email("unverified@microfin.com")
                .password("encodedPass")
                .role(Role.LOAN_OFFICER)
                .status("PENDING_VERIFICATION")
                .build();

        when(userRepository.findByEmail("unverified@microfin.com")).thenReturn(Optional.of(unverifiedUser));
        when(passwordEncoder.matches("myPassword", "encodedPass")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> {
            authService.login("unverified@microfin.com", "myPassword");
        });

        assertTrue(ex.getMessage().contains("Your email has not been verified yet"));
    }
}

