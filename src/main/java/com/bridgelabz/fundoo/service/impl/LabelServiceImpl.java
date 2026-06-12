package com.bridgelabz.fundoo.service.impl;

import com.bridgelabz.fundoo.dto.request.LabelRequest;
import com.bridgelabz.fundoo.dto.response.LabelResponse;
import com.bridgelabz.fundoo.dto.response.NoteResponse;
import com.bridgelabz.fundoo.entity.CollaboratorRole;
import com.bridgelabz.fundoo.entity.Label;
import com.bridgelabz.fundoo.entity.Note;
import com.bridgelabz.fundoo.entity.User;
import com.bridgelabz.fundoo.exception.ResourceNotFoundException;
import com.bridgelabz.fundoo.exception.UserAlreadyExistsException;
import com.bridgelabz.fundoo.mapper.FundooMapper;
import com.bridgelabz.fundoo.repository.LabelRepository;
import com.bridgelabz.fundoo.repository.NoteRepository;
import com.bridgelabz.fundoo.repository.NoteSpecification;
import com.bridgelabz.fundoo.service.interfaces.LabelService;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LabelServiceImpl implements LabelService {

    private static final Logger log = LoggerFactory.getLogger(LabelServiceImpl.class);

    private final LabelRepository labelRepository;
    private final NoteRepository noteRepository;
    private final UserService userService;
    private final FundooMapper fundooMapper;

    public LabelServiceImpl(LabelRepository labelRepository, NoteRepository noteRepository, UserService userService, FundooMapper fundooMapper) {
        this.labelRepository = labelRepository;
        this.noteRepository = noteRepository;
        this.userService = userService;
        this.fundooMapper = fundooMapper;
    }

    private void checkLabelOwnership(Label label, User user) {
        if (!label.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedException("You do not own this label.");
        }
    }

    private void checkNoteWritePermission(Note note, User user) {
        if (note.getOwner().getId().equals(user.getId())) {
            return;
        }
        boolean isEditor = note.getCollaborators().stream()
                .anyMatch(c -> c.getUser().getId().equals(user.getId()) && c.getRole() == CollaboratorRole.EDITOR);
        if (!isEditor) {
            throw new AccessDeniedException("You do not have permission to modify this note.");
        }
    }

    @Override
    @Transactional
    public LabelResponse createLabel(LabelRequest labelRequest) {
        User currentUser = userService.getAuthenticatedUser();

        // Check if label already exists for this user
        labelRepository.findByNameAndUserId(labelRequest.getName(), currentUser.getId())
                .ifPresent(l -> {
                    throw new UserAlreadyExistsException("Label with name '" + labelRequest.getName() + "' already exists.");
                });

        Label label = Label.builder()
                .name(labelRequest.getName())
                .user(currentUser)
                .build();

        Label saved = labelRepository.save(label);
        log.info("Created label '{}' for user: {}", saved.getName(), currentUser.getEmail());
        return fundooMapper.toLabelResponse(saved);
    }

    @Override
    public List<LabelResponse> getAllLabels() {
        User currentUser = userService.getAuthenticatedUser();
        List<Label> labels = labelRepository.findByUserId(currentUser.getId());
        return labels.stream().map(fundooMapper::toLabelResponse).collect(Collectors.toList());
    }

    @Override
    public LabelResponse getLabelById(Long id) {
        User currentUser = userService.getAuthenticatedUser();
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found with ID: " + id));

        checkLabelOwnership(label, currentUser);
        return fundooMapper.toLabelResponse(label);
    }

    @Override
    @Transactional
    public LabelResponse updateLabel(Long id, LabelRequest labelRequest) {
        User currentUser = userService.getAuthenticatedUser();
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found with ID: " + id));

        checkLabelOwnership(label, currentUser);

        // Check if name is being changed and if new name already exists
        if (!label.getName().equalsIgnoreCase(labelRequest.getName())) {
            labelRepository.findByNameAndUserId(labelRequest.getName(), currentUser.getId())
                    .ifPresent(l -> {
                        throw new UserAlreadyExistsException("Label with name '" + labelRequest.getName() + "' already exists.");
                    });
        }

        label.setName(labelRequest.getName());
        Label updated = labelRepository.save(label);
        log.info("Updated label ID: {} to name '{}'", id, updated.getName());
        return fundooMapper.toLabelResponse(updated);
    }

    @Override
    @Transactional
    public void deleteLabel(Long id) {
        User currentUser = userService.getAuthenticatedUser();
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found with ID: " + id));

        checkLabelOwnership(label, currentUser);

        // Clear label from notes first to avoid constraint issues (many-to-many)
        label.getNotes().forEach(note -> note.getLabels().remove(label));
        labelRepository.delete(label);
        log.info("Deleted label with ID: {}", id);
    }

    @Override
    @Transactional
    public NoteResponse attachLabel(Long noteId, Long labelId) {
        User currentUser = userService.getAuthenticatedUser();
        
        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with ID: " + noteId));
        checkNoteWritePermission(note, currentUser);

        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found with ID: " + labelId));
        checkLabelOwnership(label, currentUser);

        note.getLabels().add(label);
        Note savedNote = noteRepository.save(note);
        log.info("Attached label {} to note {}", labelId, noteId);
        return fundooMapper.toNoteResponse(savedNote);
    }

    @Override
    @Transactional
    public NoteResponse detachLabel(Long noteId, Long labelId) {
        User currentUser = userService.getAuthenticatedUser();

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with ID: " + noteId));
        checkNoteWritePermission(note, currentUser);

        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found with ID: " + labelId));
        checkLabelOwnership(label, currentUser);

        note.getLabels().remove(label);
        Note savedNote = noteRepository.save(note);
        log.info("Detached label {} from note {}", labelId, noteId);
        return fundooMapper.toNoteResponse(savedNote);
    }

    @Override
    public Page<NoteResponse> getNotesByLabel(Long labelId, int page, int size, String sortBy, String direction) {
        User currentUser = userService.getAuthenticatedUser();

        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new ResourceNotFoundException("Label not found with ID: " + labelId));
        checkLabelOwnership(label, currentUser);

        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<Note> notes = noteRepository.findAll(
                NoteSpecification.getNotesByLabelId(currentUser.getId(), labelId),
                pageable
        );

        return notes.map(fundooMapper::toNoteResponse);
    }
}
