package com.bridgelabz.fundoo.service.impl;

import com.bridgelabz.fundoo.dto.request.CollaboratorRequest;
import com.bridgelabz.fundoo.dto.response.CollaboratorResponse;
import com.bridgelabz.fundoo.entity.Collaborator;
import com.bridgelabz.fundoo.entity.CollaboratorRole;
import com.bridgelabz.fundoo.entity.Note;
import com.bridgelabz.fundoo.entity.User;
import com.bridgelabz.fundoo.exception.ResourceNotFoundException;
import com.bridgelabz.fundoo.exception.UserAlreadyExistsException;
import com.bridgelabz.fundoo.mapper.FundooMapper;
import com.bridgelabz.fundoo.repository.CollaboratorRepository;
import com.bridgelabz.fundoo.repository.NoteRepository;
import com.bridgelabz.fundoo.repository.UserRepository;
import com.bridgelabz.fundoo.service.interfaces.CollaboratorService;
import com.bridgelabz.fundoo.service.interfaces.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CollaboratorServiceImpl implements CollaboratorService {

    private static final Logger log = LoggerFactory.getLogger(CollaboratorServiceImpl.class);

    private final CollaboratorRepository collaboratorRepository;
    private final NoteRepository noteRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final FundooMapper fundooMapper;

    public CollaboratorServiceImpl(CollaboratorRepository collaboratorRepository, NoteRepository noteRepository, UserRepository userRepository, UserService userService, FundooMapper fundooMapper) {
        this.collaboratorRepository = collaboratorRepository;
        this.noteRepository = noteRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.fundooMapper = fundooMapper;
    }

    private void checkNoteOwner(Note note, User user) {
        if (!note.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("Only the note owner can manage collaborators.");
        }
    }

    private void checkReadPermission(Note note, User user) {
        if (note.getOwner().getId().equals(user.getId())) {
            return;
        }
        boolean isCollaborator = note.getCollaborators().stream()
                .anyMatch(c -> c.getUser().getId().equals(user.getId()));
        if (!isCollaborator) {
            throw new AccessDeniedException("You do not have permission to access this note.");
        }
    }

    @Override
    @Transactional
    public CollaboratorResponse addCollaborator(Long noteId, CollaboratorRequest collaboratorRequest) {
        User currentUser = userService.getAuthenticatedUser();

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with ID: " + noteId));
        checkNoteOwner(note, currentUser);

        User targetUser = userRepository.findByEmailAndDeletedFalse(collaboratorRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + collaboratorRequest.getEmail()));

        if (targetUser.getId().equals(currentUser.getId())) {
            throw new IllegalArgumentException("You cannot add yourself as a collaborator on your own note.");
        }

        collaboratorRepository.findByNoteIdAndUserId(noteId, targetUser.getId())
                .ifPresent(c -> {
                    throw new UserAlreadyExistsException("User is already collaborating on this note.");
                });

        CollaboratorRole role;
        try {
            role = CollaboratorRole.valueOf(collaboratorRequest.getRole().toUpperCase());
        } catch (IllegalArgumentException e) {
            role = CollaboratorRole.VIEWER; // Default fallback
        }

        Collaborator collaborator = Collaborator.builder()
                .note(note)
                .user(targetUser)
                .role(role)
                .build();

        Collaborator saved = collaboratorRepository.save(collaborator);
        log.info("Added user {} as collaborator to note ID: {}", targetUser.getEmail(), noteId);
        return fundooMapper.toCollaboratorResponse(saved);
    }

    @Override
    public List<CollaboratorResponse> getCollaboratorsOfNote(Long noteId) {
        User currentUser = userService.getAuthenticatedUser();
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with ID: " + noteId));
        checkReadPermission(note, currentUser);

        List<Collaborator> collaborators = collaboratorRepository.findByNoteId(noteId);
        return collaborators.stream().map(fundooMapper::toCollaboratorResponse).collect(Collectors.toList());
    }

    @Override
    public CollaboratorResponse getCollaboratorById(Long id) {
        User currentUser = userService.getAuthenticatedUser();
        Collaborator collaborator = collaboratorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collaborator not found with ID: " + id));

        // Allowed only if note owner OR collaborator user themselves
        if (!collaborator.getNote().getOwner().getId().equals(currentUser.getId()) &&
                !collaborator.getUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You do not have permission to view this collaborator record.");
        }

        return fundooMapper.toCollaboratorResponse(collaborator);
    }

    @Override
    @Transactional
    public void removeCollaborator(Long noteId, Long collaboratorId) {
        User currentUser = userService.getAuthenticatedUser();

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with ID: " + noteId));
        checkNoteOwner(note, currentUser);

        Collaborator collaborator = collaboratorRepository.findById(collaboratorId)
                .orElseThrow(() -> new ResourceNotFoundException("Collaborator not found with ID: " + collaboratorId));

        if (!collaborator.getNote().getId().equals(noteId)) {
            throw new IllegalArgumentException("Collaborator record does not match the note ID.");
        }

        collaboratorRepository.delete(collaborator);
        log.info("Removed collaborator ID: {} from note ID: {}", collaboratorId, noteId);
    }
}
