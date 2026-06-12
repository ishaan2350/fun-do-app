package com.bridgelabz.fundoo.repository;

import com.bridgelabz.fundoo.entity.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long>, JpaSpecificationExecutor<Note> {
    
    // Find all notes of a user, filtering by trashed and archived status. Pinned notes should come first (ordered in controller or service pageable/sorting).
    Page<Note> findByOwnerIdAndTrashedAndArchived(Long ownerId, boolean trashed, boolean archived, Pageable pageable);
    
    // Find trashed notes of a user
    Page<Note> findByOwnerIdAndTrashed(Long ownerId, boolean trashed, Pageable pageable);

    // Find notes for purging (trashed and updated_at before 30 days ago)
    List<Note> findByTrashedTrueAndUpdatedAtBefore(LocalDateTime threshold);
    
    // Also support getting notes where user is owner OR a collaborator (for shared view)
    @Query("SELECT n FROM Note n LEFT JOIN n.collaborators c WHERE (n.owner.id = :userId OR c.user.id = :userId) AND n.trashed = :trashed AND n.archived = :archived")
    Page<Note> findByOwnerOrCollaborator(@Param("userId") Long userId, @Param("trashed") boolean trashed, @Param("archived") boolean archived, Pageable pageable);
}
