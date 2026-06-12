package com.bridgelabz.fundoo.repository;

import com.bridgelabz.fundoo.entity.Collaborator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollaboratorRepository extends JpaRepository<Collaborator, Long> {
    List<Collaborator> findByNoteId(Long noteId);
    Optional<Collaborator> findByNoteIdAndUserId(Long noteId, Long userId);
}
