package com.bridgelabz.fundoo.service.impl;

import com.bridgelabz.fundoo.service.interfaces.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);
    private final JavaMailSender mailSender;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendVerificationEmail(String to, String token) {
        String verificationUrl = "http://localhost:8080/api/v1/users/verify-email?token=" + token;
        String message = "Please click the following link to activate your Fundoo Notes account:\n" + verificationUrl;
        log.info("[DEVELOPER MODE] Verification token for {}: {}", to, token);
        sendEmail(to, "Activate Your Fundoo Notes Account", message);
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
            mailMessage.setFrom("no-reply@fundoonotes.com");
            mailSender.send(mailMessage);
            log.info("Email successfully sent to {}", to);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}
