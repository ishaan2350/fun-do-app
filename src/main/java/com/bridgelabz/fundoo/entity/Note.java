package com.bridgelabz.fundoo.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "notes")
public class Note {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 50)
    private String color = "white";

    @Column(nullable = false)
    private boolean pinned = false;

    @Column(nullable = false)
    private boolean archived = false;

    @Column(nullable = false)
    private boolean trashed = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "note_labels",
        joinColumns = @JoinColumn(name = "note_id"),
        inverseJoinColumns = @JoinColumn(name = "label_id")
    )
    private Set<Label> labels = new HashSet<>();

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Collaborator> collaborators = new ArrayList<>();

    @OneToMany(mappedBy = "note", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Reminder> reminders = new ArrayList<>();

    // Constructors
    public Note() {
    }

    public Note(Long id, String title, String description, String color, boolean pinned, boolean archived, boolean trashed, User owner, LocalDateTime createdAt, LocalDateTime updatedAt, Set<Label> labels, List<Collaborator> collaborators, List<Reminder> reminders) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.color = color;
        this.pinned = pinned;
        this.archived = archived;
        this.trashed = trashed;
        this.owner = owner;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.labels = labels;
        this.collaborators = collaborators;
        this.reminders = reminders;
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

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Set<Label> getLabels() { return labels; }
    public void setLabels(Set<Label> labels) { this.labels = labels; }

    public List<Collaborator> getCollaborators() { return collaborators; }
    public void setCollaborators(List<Collaborator> collaborators) { this.collaborators = collaborators; }

    public List<Reminder> getReminders() { return reminders; }
    public void setReminders(List<Reminder> reminders) { this.reminders = reminders; }

    // Manual Builder
    public static NoteBuilder builder() {
        return new NoteBuilder();
    }

    public static class NoteBuilder {
        private Long id;
        private String title;
        private String description;
        private String color = "white";
        private boolean pinned;
        private boolean archived;
        private boolean trashed;
        private User owner;

        public NoteBuilder id(Long id) { this.id = id; return this; }
        public NoteBuilder title(String title) { this.title = title; return this; }
        public NoteBuilder description(String description) { this.description = description; return this; }
        public NoteBuilder color(String color) { this.color = color; return this; }
        public NoteBuilder pinned(boolean pinned) { this.pinned = pinned; return this; }
        public NoteBuilder archived(boolean archived) { this.archived = archived; return this; }
        public NoteBuilder trashed(boolean trashed) { this.trashed = trashed; return this; }
        public NoteBuilder owner(User owner) { this.owner = owner; return this; }

        public Note build() {
            Note note = new Note();
            note.setId(this.id);
            note.setTitle(this.title);
            note.setDescription(this.description);
            if (this.color != null) {
                note.setColor(this.color);
            }
            note.setPinned(this.pinned);
            note.setArchived(this.archived);
            note.setTrashed(this.trashed);
            note.setOwner(this.owner);
            return note;
        }
    }
}
