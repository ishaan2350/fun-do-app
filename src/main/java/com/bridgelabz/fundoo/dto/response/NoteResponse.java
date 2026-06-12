package com.bridgelabz.fundoo.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public class NoteResponse {
    private Long id;
    private String title;
    private String description;
    private String color;
    private boolean pinned;
    private boolean archived;
    private boolean trashed;
    private Long ownerId;
    private Set<LabelResponse> labels;
    private List<CollaboratorResponse> collaborators;
    private List<ReminderResponse> reminders;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public NoteResponse() {
    }

    public NoteResponse(Long id, String title, String description, String color, boolean pinned, boolean archived, boolean trashed, Long ownerId, Set<LabelResponse> labels, List<CollaboratorResponse> collaborators, List<ReminderResponse> reminders, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.color = color;
        this.pinned = pinned;
        this.archived = archived;
        this.trashed = trashed;
        this.ownerId = ownerId;
        this.labels = labels;
        this.collaborators = collaborators;
        this.reminders = reminders;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }

    public boolean isArchived() { return archived; }
    public void setArchived(boolean archived) { this.archived = archived; }

    public boolean isTrashed() { return trashed; }
    public void setTrashed(boolean trashed) { this.trashed = trashed; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public Set<LabelResponse> getLabels() { return labels; }
    public void setLabels(Set<LabelResponse> labels) { this.labels = labels; }

    public List<CollaboratorResponse> getCollaborators() { return collaborators; }
    public void setCollaborators(List<CollaboratorResponse> collaborators) { this.collaborators = collaborators; }

    public List<ReminderResponse> getReminders() { return reminders; }
    public void setReminders(List<ReminderResponse> reminders) { this.reminders = reminders; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Manual Builder
    public static NoteResponseBuilder builder() {
        return new NoteResponseBuilder();
    }

    public static class NoteResponseBuilder {
        private Long id;
        private String title;
        private String description;
        private String color;
        private boolean pinned;
        private boolean archived;
        private boolean trashed;
        private Long ownerId;
        private Set<LabelResponse> labels;
        private List<CollaboratorResponse> collaborators;
        private List<ReminderResponse> reminders;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public NoteResponseBuilder id(Long id) { this.id = id; return this; }
        public NoteResponseBuilder title(String title) { this.title = title; return this; }
        public NoteResponseBuilder description(String description) { this.description = description; return this; }
        public NoteResponseBuilder color(String color) { this.color = color; return this; }
        public NoteResponseBuilder pinned(boolean pinned) { this.pinned = pinned; return this; }
        public NoteResponseBuilder archived(boolean archived) { this.archived = archived; return this; }
        public NoteResponseBuilder trashed(boolean trashed) { this.trashed = trashed; return this; }
        public NoteResponseBuilder ownerId(Long ownerId) { this.ownerId = ownerId; return this; }
        public NoteResponseBuilder labels(Set<LabelResponse> labels) { this.labels = labels; return this; }
        public NoteResponseBuilder collaborators(List<CollaboratorResponse> collaborators) { this.collaborators = collaborators; return this; }
        public NoteResponseBuilder reminders(List<ReminderResponse> reminders) { this.reminders = reminders; return this; }
        public NoteResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public NoteResponseBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public NoteResponse build() {
            return new NoteResponse(id, title, description, color, pinned, archived, trashed, ownerId, labels, collaborators, reminders, createdAt, updatedAt);
        }
    }
}
