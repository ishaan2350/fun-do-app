package com.bridgelabz.fundoo.dto.response;

import java.time.LocalDateTime;

public class CollaboratorResponse {
    private Long id;
    private Long noteId;
    private Long userId;
    private String userEmail;
    private String role;
    private LocalDateTime addedAt;

    // Constructors
    public CollaboratorResponse() {
    }

    public CollaboratorResponse(Long id, Long noteId, Long userId, String userEmail, String role, LocalDateTime addedAt) {
        this.id = id;
        this.noteId = noteId;
        this.userId = userId;
        this.userEmail = userEmail;
        this.role = role;
        this.addedAt = addedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getNoteId() { return noteId; }
    public void setNoteId(Long noteId) { this.noteId = noteId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }

    // Manual Builder
    public static CollaboratorResponseBuilder builder() {
        return new CollaboratorResponseBuilder();
    }

    public static class CollaboratorResponseBuilder {
        private Long id;
        private Long noteId;
        private Long userId;
        private String userEmail;
        private String role;
        private LocalDateTime addedAt;

        public CollaboratorResponseBuilder id(Long id) { this.id = id; return this; }
        public CollaboratorResponseBuilder noteId(Long noteId) { this.noteId = noteId; return this; }
        public CollaboratorResponseBuilder userId(Long userId) { this.userId = userId; return this; }
        public CollaboratorResponseBuilder userEmail(String userEmail) { this.userEmail = userEmail; return this; }
        public CollaboratorResponseBuilder role(String role) { this.role = role; return this; }
        public CollaboratorResponseBuilder addedAt(LocalDateTime addedAt) { this.addedAt = addedAt; return this; }

        public CollaboratorResponse build() {
            return new CollaboratorResponse(id, noteId, userId, userEmail, role, addedAt);
        }
    }
}
