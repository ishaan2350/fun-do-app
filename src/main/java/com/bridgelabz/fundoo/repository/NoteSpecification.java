package com.bridgelabz.fundoo.repository;

import com.bridgelabz.fundoo.entity.Collaborator;
import com.bridgelabz.fundoo.entity.Note;
import com.bridgelabz.fundoo.entity.Label;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class NoteSpecification {

    public static Specification<Note> searchNotes(Long userId, String query) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. User must be owner OR collaborator
            // Join with collaborators
            Join<Note, Collaborator> collaboratorJoin = root.join("collaborators", JoinType.LEFT);
            Predicate isOwner = cb.equal(root.get("owner").get("id"), userId);
            Predicate isCollaborator = cb.equal(collaboratorJoin.get("user").get("id"), userId);
            predicates.add(cb.or(isOwner, isCollaborator));

            // 2. Note must NOT be trashed
            predicates.add(cb.isFalse(root.get("trashed")));

            // 3. Search query matches title or description
            if (query != null && !query.trim().isEmpty()) {
                String searchPattern = "%" + query.toLowerCase() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), searchPattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), searchPattern);
                predicates.add(cb.or(titleMatch, descMatch));
            }

            // Ensure distinct results because of join
            criteriaQuery.distinct(true);

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Note> filterNotes(Long userId, Boolean pinned, Boolean archived, Boolean trashed) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // User must be owner OR collaborator
            Join<Note, Collaborator> collaboratorJoin = root.join("collaborators", JoinType.LEFT);
            Predicate isOwner = cb.equal(root.get("owner").get("id"), userId);
            Predicate isCollaborator = cb.equal(collaboratorJoin.get("user").get("id"), userId);
            predicates.add(cb.or(isOwner, isCollaborator));

            if (pinned != null) {
                predicates.add(cb.equal(root.get("pinned"), pinned));
            }
            if (archived != null) {
                predicates.add(cb.equal(root.get("archived"), archived));
            }
            if (trashed != null) {
                predicates.add(cb.equal(root.get("trashed"), trashed));
            }

            criteriaQuery.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static Specification<Note> getNotesByLabelId(Long userId, Long labelId) {
        return (root, criteriaQuery, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. User access check (owner or collaborator)
            Join<Note, Collaborator> collaboratorJoin = root.join("collaborators", JoinType.LEFT);
            Predicate isOwner = cb.equal(root.get("owner").get("id"), userId);
            Predicate isCollaborator = cb.equal(collaboratorJoin.get("user").get("id"), userId);
            predicates.add(cb.or(isOwner, isCollaborator));

            // 2. Note must NOT be trashed
            predicates.add(cb.isFalse(root.get("trashed")));

            // 3. Matches label ID
            Join<Note, Label> labelJoin = root.join("labels");
            predicates.add(cb.equal(labelJoin.get("id"), labelId));

            criteriaQuery.distinct(true);
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
