package com.bridgelabz.fundoo.dto.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class ReminderRequest {

    @NotNull(message = "Reminder time is required")
    private LocalDateTime remindAt;

    // Constructors
    public ReminderRequest() {
    }

    public ReminderRequest(LocalDateTime remindAt) {
        this.remindAt = remindAt;
    }

    // Getters and Setters
    public LocalDateTime getRemindAt() { return remindAt; }
    public void setRemindAt(LocalDateTime remindAt) { this.remindAt = remindAt; }
}
