package com.bridgelabz.fundoo.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "labels", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name", "user_id"})
})
public class Label {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToMany(mappedBy = "labels")
    private Set<Note> notes = new HashSet<>();

    // Constructors
    public Label() {
    }

    public Label(Long id, String name, User user, LocalDateTime createdAt, LocalDateTime updatedAt, Set<Note> notes) {
        this.id = id;
        this.name = name;
        this.user = user;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.notes = notes;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public Set<Note> getNotes() { return notes; }
    public void setNotes(Set<Note> notes) { this.notes = notes; }

    // Manual Builder
    public static LabelBuilder builder() {
        return new LabelBuilder();
    }

    public static class LabelBuilder {
        private Long id;
        private String name;
        private User user;

        public LabelBuilder id(Long id) { this.id = id; return this; }
        public LabelBuilder name(String name) { this.name = name; return this; }
        public LabelBuilder user(User user) { this.user = user; return this; }

        public Label build() {
            Label label = new Label();
            label.setId(this.id);
            label.setName(this.name);
            label.setUser(this.user);
            return label;
        }
    }
}
