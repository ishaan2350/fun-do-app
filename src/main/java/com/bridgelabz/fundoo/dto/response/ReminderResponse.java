package com.bridgelabz.fundoo.dto.response;

import java.time.LocalDateTime;

public class ReminderResponse {
    private Long id;
    private Long noteId;
    private LocalDateTime remindAt;
    private boolean notified;
    private LocalDateTime createdAt;

    // Constructors
    public ReminderResponse() {
    }

    public ReminderResponse(Long id, Long noteId, LocalDateTime remindAt, boolean notified, LocalDateTime createdAt) {
        this.id = id;
        this.noteId = noteId;
        this.remindAt = remindAt;
        this.notified = notified;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getNoteId() { return noteId; }
    public void setNoteId(Long noteId) { this.noteId = noteId; }

    public LocalDateTime getRemindAt() { return remindAt; }
    public void setRemindAt(LocalDateTime remindAt) { this.remindAt = remindAt; }

    public boolean isNotified() { return notified; }
    public void setNotified(boolean notified) { this.notified = notified; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Manual Builder
    public static ReminderResponseBuilder builder() {
        return new ReminderResponseBuilder();
    }

    public static class ReminderResponseBuilder {
        private Long id;
        private Long noteId;
        private LocalDateTime remindAt;
        private boolean notified;
        private LocalDateTime createdAt;

        public ReminderResponseBuilder id(Long id) { this.id = id; return this; }
        public ReminderResponseBuilder noteId(Long noteId) { this.noteId = noteId; return this; }
        public ReminderResponseBuilder remindAt(LocalDateTime remindAt) { this.remindAt = remindAt; return this; }
        public ReminderResponseBuilder notified(boolean notified) { this.notified = notified; return this; }
        public ReminderResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }

        public ReminderResponse build() {
            return new ReminderResponse(id, noteId, remindAt, notified, createdAt);
        }
    }
}
