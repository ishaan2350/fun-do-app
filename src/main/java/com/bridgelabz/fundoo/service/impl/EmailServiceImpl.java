package com.bridgelabz.fundoo.service.impl;

import com.bridgelabz.fundoo.service.interfaces.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendVerificationEmail(String to, String token) {
        String verificationUrl = "http://localhost:8080/api/v1/users/verify-email?token=" + token;
        String message = "Hello,\n\n" +
                "Welcome to Fundoo!\n\n" +
                "You have successfully registered an account in the Fundoo application.\n\n" +
                "To complete your registration and activate your account, please click the verification link below:\n" +
                verificationUrl + "\n\n" +
                "Best regards,\n" +
                "Fundoo Application Owner";
        
        log.info("[DEVELOPER MODE] Verification token for {}: {}", to, token);
        sendEmail(to, "Welcome to Fundoo - Registration Successful", message);
    }

    @Override
    public void sendForgotPasswordEmail(String to, String token) {
        String resetUrl = "http://localhost:8080/api/v1/users/reset-password?token=" + token;
        String message = "Please use the following token or link to reset your password:\n\nToken: " + token + "\n\nLink: " + resetUrl;
        log.info("[DEVELOPER MODE] Reset token for {}: {}", to, token);
        sendEmail(to, "Reset Your Fundoo Notes Password", message);
    }

    @Override
    public void sendReminderEmail(String to, String noteTitle, String noteDescription) {
        String message = "Reminder for your note:\n\nTitle: " + noteTitle + "\nDescription: " + noteDescription;
        sendEmail(to, "Fundoo Note Reminder: " + (noteTitle != null ? noteTitle : "Untitled"), message);
    }

    private void sendEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo(to);
            mailMessage.setSubject(subject);
            mailMessage.setText(text);
            // Dynamically set From to the owner's configured email address
            mailMessage.setFrom(fromEmail);
            mailSender.send(mailMessage);
            log.info("Email successfully sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
