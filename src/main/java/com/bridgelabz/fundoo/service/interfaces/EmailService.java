package com.bridgelabz.fundoo.service.interfaces;

public interface EmailService {
    void sendVerificationEmail(String to, String token);
    void sendForgotPasswordEmail(String to, String token);
    void sendReminderEmail(String to, String noteTitle, String noteDescription);
}
