package com.bridgelabz.fundoo.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "collaborators", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"note_id", "user_id"})
})
public class Collaborator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "note_id", nullable = false)
    private Note note;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, columnDefinition = "VARCHAR(20)")
    private CollaboratorRole role = CollaboratorRole.VIEWER;

    @CreationTimestamp
    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;

    // Constructors
    public Collaborator() {
    }

    public Collaborator(Long id, Note note, User user, CollaboratorRole role, LocalDateTime addedAt) {
        this.id = id;
        this.note = note;
        this.user = user;
        this.role = role;
        this.addedAt = addedAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Note getNote() { return note; }
    public void setNote(Note note) { this.note = note; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public CollaboratorRole getRole() { return role; }
    public void setRole(CollaboratorRole role) { this.role = role; }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }

    // Manual Builder
    public static CollaboratorBuilder builder() {
        return new CollaboratorBuilder();
    }

    public static class CollaboratorBuilder {
        private Long id;
        private Note note;
        private User user;
        private CollaboratorRole role = CollaboratorRole.VIEWER;

        public CollaboratorBuilder id(Long id) { this.id = id; return this; }
        public CollaboratorBuilder note(Note note) { this.note = note; return this; }
        public CollaboratorBuilder user(User user) { this.user = user; return this; }
        public CollaboratorBuilder role(CollaboratorRole role) { this.role = role; return this; }

        public Collaborator build() {
            Collaborator collaborator = new Collaborator();
            collaborator.setId(this.id);
            collaborator.setNote(this.note);
            collaborator.setUser(this.user);
            if (this.role != null) {
                collaborator.setRole(this.role);
            }
            return collaborator;
        }
    }
}
