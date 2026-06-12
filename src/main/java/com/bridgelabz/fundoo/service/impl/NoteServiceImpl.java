package com.bridgelabz.fundoo.service.impl;

import com.bridgelabz.fundoo.dto.request.NoteRequest;
import com.bridgelabz.fundoo.dto.response.NoteResponse;
import com.bridgelabz.fundoo.entity.CollaboratorRole;
import com.bridgelabz.fundoo.entity.Note;
import com.bridgelabz.fundoo.entity.User;
import com.bridgelabz.fundoo.exception.ResourceNotFoundException;
import com.bridgelabz.fundoo.mapper.FundooMapper;
import com.bridgelabz.fundoo.repository.NoteRepository;
import com.bridgelabz.fundoo.repository.NoteSpecification;
import com.bridgelabz.fundoo.service.interfaces.NoteService;
import com.bridgelabz.fundoo.service.interfaces.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class NoteServiceImpl implements NoteService {

    private static final Logger log = LoggerFactory.getLogger(NoteServiceImpl.class);

    private final NoteRepository noteRepository;
    private final UserService userService;
    private final FundooMapper fundooMapper;

    public NoteServiceImpl(NoteRepository noteRepository, UserService userService, FundooMapper fundooMapper) {
        this.noteRepository = noteRepository;
        this.userService = userService;
        this.fundooMapper = fundooMapper;
    }

    private static final Set<String> ALLOWED_COLORS = Set.of(
            "white", "yellow", "green", "blue", "red", "purple", "orange", "pink"
    );

    private void checkReadPermission(Note note, User user) {
        if (note.getOwner().getId().equals(user.getId())) {
            return;
        }
        boolean isCollaborator = note.getCollaborators().stream()
                .anyMatch(c -> c.getUser().getId().equals(user.getId()));
        if (!isCollaborator) {
            throw new AccessDeniedException("You do not have permission to view this note.");
        }
    }

    private void checkWritePermission(Note note, User user) {
        if (note.getOwner().getId().equals(user.getId())) {
            return;
        }
        boolean isEditor = note.getCollaborators().stream()
                .anyMatch(c -> c.getUser().getId().equals(user.getId()) && c.getRole() == CollaboratorRole.EDITOR);
        if (!isEditor) {
            throw new AccessDeniedException("You do not have permission to modify this note. Only the owner or editors can modify.");
        }
    }

    private void checkOwnerPermission(Note note, User user) {
        if (!note.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("Only the note owner can perform this operation.");
        }
    }

    private String validateAndGetColor(String color) {
        if (color == null || !ALLOWED_COLORS.contains(color.toLowerCase())) {
            return "white";
        }
        return color.toLowerCase();
    }

    @Override
    @Transactional
    public NoteResponse createNote(NoteRequest noteRequest) {
        User currentUser = userService.getAuthenticatedUser();
        
        Note note = Note.builder()
                .title(noteRequest.getTitle())
                .description(noteRequest.getDescription())
                .color(validateAndGetColor(noteRequest.getColor()))
                .pinned(noteRequest.isPinned())
                .archived(noteRequest.isArchived())
                .trashed(noteRequest.isTrashed())
                .owner(currentUser)
                .build();

        Note savedNote = noteRepository.save(note);
        log.info("Created new note with ID: {} for user: {}", savedNote.getId(), currentUser.getEmail());
        return fundooMapper.toNoteResponse(savedNote);
    }

    @Override
    public NoteResponse getNoteById(Long id) {
        User currentUser = userService.getAuthenticatedUser();
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with ID: " + id));

        checkReadPermission(note, currentUser);
        return fundooMapper.toNoteResponse(note);
    }

    @Override
    @Transactional
    public NoteResponse updateNote(Long id, NoteRequest noteRequest) {
        User currentUser = userService.getAuthenticatedUser();
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with ID: " + id));

        checkWritePermission(note, currentUser);

        note.setTitle(noteRequest.getTitle());
        note.setDescription(noteRequest.getDescription());
        note.setColor(validateAndGetColor(noteRequest.getColor()));
        note.setPinned(noteRequest.isPinned());
        note.setArchived(noteRequest.isArchived());
        note.setTrashed(noteRequest.isTrashed());

        Note updatedNote = noteRepository.save(note);
        log.info("Updated note with ID: {}", updatedNote.getId());
        return fundooMapper.toNoteResponse(updatedNote);
    }

    @Override
    @Transactional
    public void deleteNote(Long id) {
        User currentUser = userService.getAuthenticatedUser();
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with ID: " + id));

        checkOwnerPermission(note, currentUser);

        if (!note.isTrashed()) {
            note.setTrashed(true);
            note.setPinned(false);
            note.setArchived(false);
            noteRepository.save(note);
            log.info("Trashed note with ID: {}", id);
        } else {
            noteRepository.delete(note);
            log.info("Permanently deleted note with ID: {}", id);
        }
    }

    @Override
    @Transactional
    public NoteResponse togglePin(Long id) {
        User currentUser = userService.getAuthenticatedUser();
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with ID: " + id));

        checkWritePermission(note, currentUser);

        note.setPinned(!note.isPinned());
        if (note.isPinned()) {
            note.setArchived(false); // A pinned note cannot be archived
        }
        
        Note updated = noteRepository.save(note);
        log.info("Toggled pin state for note {}: now {}", id, updated.isPinned());
        return fundooMapper.toNoteResponse(updated);
    }

    @Override
    @Transactional
    public NoteResponse toggleArchive(Long id) {
        User currentUser = userService.getAuthenticatedUser();
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with ID: " + id));

        checkWritePermission(note, currentUser);

        note.setArchived(!note.isArchived());
        if (note.isArchived()) {
            note.setPinned(false); // An archived note cannot be pinned
        }

        Note updated = noteRepository.save(note);
        log.info("Toggled archive state for note {}: now {}", id, updated.isArchived());
        return fundooMapper.toNoteResponse(updated);
    }

    @Override
    @Transactional
    public NoteResponse toggleTrash(Long id) {
        User currentUser = userService.getAuthenticatedUser();
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with ID: " + id));

        checkOwnerPermission(note, currentUser);

        note.setTrashed(!note.isTrashed());
        if (note.isTrashed()) {
            note.setPinned(false);
            note.setArchived(false);
        }

        Note updated = noteRepository.save(note);
        log.info("Toggled trash state for note {}: now {}", id, updated.isTrashed());
        return fundooMapper.toNoteResponse(updated);
    }

    @Override
    @Transactional
    public NoteResponse updateColor(Long id, String color) {
        User currentUser = userService.getAuthenticatedUser();
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with ID: " + id));

        checkWritePermission(note, currentUser);

        note.setColor(validateAndGetColor(color));
        Note updated = noteRepository.save(note);
        log.info("Updated color for note {} to {}", id, updated.getColor());
        return fundooMapper.toNoteResponse(updated);
    }

    @Override
    public Page<NoteResponse> getAllNotes(int page, int size, String sortBy, String direction, Boolean pinned, Boolean archived, Boolean trashed) {
        User currentUser = userService.getAuthenticatedUser();
        
        // Pinned notes appear first if not explicitly sorted otherwise
        Sort sort = Sort.by(direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        if (sortBy.equals("createdAt") || sortBy.equals("id")) {
            sort = Sort.by(Sort.Order.desc("pinned"), direction.equalsIgnoreCase("desc") ? Sort.Order.desc(sortBy) : Sort.Order.asc(sortBy));
        }
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Note> notes = noteRepository.findAll(
                NoteSpecification.filterNotes(currentUser.getId(), pinned, archived, trashed), 
                pageable
        );
        
        return notes.map(fundooMapper::toNoteResponse);
    }

    @Override
    public Page<NoteResponse> searchNotes(String query, int page, int size, String sortBy, String direction) {
        User currentUser = userService.getAuthenticatedUser();
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<Note> notes = noteRepository.findAll(
                NoteSpecification.searchNotes(currentUser.getId(), query),
                pageable
        );

        return notes.map(fundooMapper::toNoteResponse);
    }

    @Override
    @Transactional
    public void purgeOldTrashedNotes() {
        LocalDateTime limit = LocalDateTime.now().minusDays(30);
        List<Note> oldTrashedNotes = noteRepository.findByTrashedTrueAndUpdatedAtBefore(limit);
        if (!oldTrashedNotes.isEmpty()) {
            noteRepository.deleteAll(oldTrashedNotes);
            log.info("Successfully purged {} notes that were trashed for more than 30 days.", oldTrashedNotes.size());
        }
    }
}
