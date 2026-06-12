package com.bridgelabz.fundoo.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "reminders")
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;

    @Column(name = "remind_at", nullable = false)
    private LocalDateTime remindAt;

    @Column(nullable = false)
    private boolean notified = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // Constructors
    public Reminder() {
    }

    public Reminder(Long id, Note note, LocalDateTime remindAt, boolean notified, LocalDateTime createdAt) {
        this.id = id;
        this.note = note;
        this.remindAt = remindAt;
        this.notified = notified;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Note getNote() { return note; }
    public void setNote(Note note) { this.note = note; }

    public LocalDateTime getRemindAt() { return remindAt; }
    public void setRemindAt(LocalDateTime remindAt) { this.remindAt = remindAt; }

    public boolean isNotified() { return notified; }
    public void setNotified(boolean notified) { this.notified = notified; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    // Manual Builder
    public static ReminderBuilder builder() {
        return new ReminderBuilder();
    }

    public static class ReminderBuilder {
        private Long id;
        private Note note;
        private LocalDateTime remindAt;
        private boolean notified;

        public ReminderBuilder id(Long id) { this.id = id; return this; }
        public ReminderBuilder note(Note note) { this.note = note; return this; }
        public ReminderBuilder remindAt(LocalDateTime remindAt) { this.remindAt = remindAt; return this; }
        public ReminderBuilder notified(boolean notified) { this.notified = notified; return this; }

        public Reminder build() {
            Reminder reminder = new Reminder();
            reminder.setId(this.id);
            reminder.setNote(this.note);
            reminder.setRemindAt(this.remindAt);
            reminder.setNotified(this.notified);
            return reminder;
        }
    }
}
