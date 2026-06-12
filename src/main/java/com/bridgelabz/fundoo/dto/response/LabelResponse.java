package com.bridgelabz.fundoo.dto.response;

import java.time.LocalDateTime;

public class LabelResponse {
    private Long id;
    private String name;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Constructors
    public LabelResponse() {
    }

    public LabelResponse(Long id, String name, Long userId, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.userId = userId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Manual Builder
    public static LabelResponseBuilder builder() {
        return new LabelResponseBuilder();
    }

    public static class LabelResponseBuilder {
        private Long id;
        private String name;
        private Long userId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;

        public LabelResponseBuilder id(Long id) { this.id = id; return this; }
        public LabelResponseBuilder name(String name) { this.name = name; return this; }
        public LabelResponseBuilder userId(Long userId) { this.userId = userId; return this; }
        public LabelResponseBuilder createdAt(LocalDateTime createdAt) { this.createdAt = createdAt; return this; }
        public LabelResponseBuilder updatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; return this; }

        public LabelResponse build() {
            return new LabelResponse(id, name, userId, createdAt, updatedAt);
        }
    }
}
