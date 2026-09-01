package com.example.microfinance_loan_system.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired(required = false)
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@microfin.com}")
    private String fromEmail;

    public boolean sendOtpEmail(String toEmail, String otp) {
        if (mailSender == null) {
            logger.warn("JavaMailSender is not initialized. OTP [{}] for [{}] logged to console only.", otp, toEmail);
            return false;
        }

        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            try {
                helper.setFrom(new jakarta.mail.internet.InternetAddress(fromEmail, "MicroFin Loan System"));
            } catch (Exception e) {
                helper.setFrom(fromEmail);
            }
            helper.setTo(toEmail);
            helper.setSubject("MicroFin — Password Reset Verification Code: " + otp);

            String htmlContent = "<div style=\"font-family: 'Segoe UI', Arial, sans-serif; max-width: 600px; margin: 0 auto; background-color: #0f172a; color: #f8fafc; padding: 32px; border-radius: 16px; border: 1px solid #1e293b;\">"
                    + "<div style=\"display: flex; align-items: center; margin-bottom: 24px;\">"
                    + "<div style=\"background: linear-gradient(135deg, #10b981, #059669); color: white; width: 42px; height: 42px; border-radius: 10px; display: inline-flex; align-items: center; justify-content: center; font-size: 22px; font-weight: bold; margin-right: 12px;\">🔑</div>"
                    + "<h2 style=\"color: #f8fafc; margin: 0; font-size: 20px;\">MicroFin Loan System</h2>"
                    + "</div>"
                    + "<h3 style=\"color: #10b981; margin-top: 0;\">Password Reset Request</h3>"
                    + "<p style=\"color: #94a3b8; font-size: 15px; line-height: 1.6;\">You requested a password reset for your MicroFin account (<strong>" + toEmail + "</strong>). Use the verification code below to complete the reset process:</p>"
                    + "<div style=\"text-align: center; margin: 28px 0;\">"
                    + "<span style=\"display: inline-block; background-color: #1e293b; color: #34d399; font-size: 32px; font-weight: 800; letter-spacing: 6px; padding: 14px 28px; border-radius: 12px; border: 2px dashed #10b981;\">"
                    + otp
                    + "</span>"
                    + "</div>"
                    + "<p style=\"color: #94a3b8; font-size: 14px;\">⏳ This code is valid for <strong>15 minutes</strong>. If you did not request this, please ignore this email or contact support immediately.</p>"
                    + "<hr style=\"border: none; border-top: 1px solid #334155; margin: 28px 0;\" />"
                    + "<p style=\"color: #64748b; font-size: 12px; margin: 0; text-align: center;\">&copy; MicroFin Loan Management System. All rights reserved.</p>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(mimeMessage);
            logger.info("Password Reset OTP email successfully dispatched to [{}]", toEmail);
            return true;
        } catch (MessagingException e) {
            logger.error("MessagingException sending OTP email to [{}]: {}", toEmail, e.getMessage(), e);
            return false;
        } catch (Exception e) {
            logger.error("Unexpected error sending email to [{}]: {}", toEmail, e.getMessage(), e);
            return false;
        }
    }
}
