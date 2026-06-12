package com.bridgelabz.fundoo.dto.request;

import jakarta.validation.constraints.Size;

public class NoteRequest {

    @Size(max = 255, message = "Title must not exceed 255 characters")
    private String title;

    private String description;

    private String color = "white";

    private boolean pinned;

    private boolean archived;

    private boolean trashed;

    // Constructors
    public NoteRequest() {
    }

    public NoteRequest(String title, String description, String color, boolean pinned, boolean archived, boolean trashed) {
        this.title = title;
        this.description = description;
        this.color = color;
        this.pinned = pinned;
        this.archived = archived;
        this.trashed = trashed;
    }

    // Getters and Setters
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

    // Manual Builder
    public static NoteRequestBuilder builder() {
        return new NoteRequestBuilder();
    }

    public static class NoteRequestBuilder {
        private String title;
        private String description;
        private String color = "white";
        private boolean pinned;
        private boolean archived;
        private boolean trashed;

        public NoteRequestBuilder title(String title) { this.title = title; return this; }
        public NoteRequestBuilder description(String description) { this.description = description; return this; }
        public NoteRequestBuilder color(String color) { this.color = color; return this; }
        public NoteRequestBuilder pinned(boolean pinned) { this.pinned = pinned; return this; }
        public NoteRequestBuilder archived(boolean archived) { this.archived = archived; return this; }
        public NoteRequestBuilder trashed(boolean trashed) { this.trashed = trashed; return this; }

        public NoteRequest build() {
            return new NoteRequest(title, description, color, pinned, archived, trashed);
        }
    }
}
