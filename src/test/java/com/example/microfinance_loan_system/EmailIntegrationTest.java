package com.example.microfinance_loan_system;

import com.example.microfinance_loan_system.service.EmailService;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Manual integration test for SMTP email dispatch")
@SpringBootTest
public class EmailIntegrationTest {

    @Autowired
    private EmailService emailService;

    @Test
    public void testSendEmail() {
        System.out.println("Testing EmailService.sendOtpEmail...");
        boolean result = emailService.sendOtpEmail("727724euit272@skcet.ac.in", "123456");
        System.out.println("Email send result: " + result);
    }
}
