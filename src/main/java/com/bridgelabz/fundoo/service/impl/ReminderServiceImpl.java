package com.bridgelabz.fundoo.service.impl;

import com.bridgelabz.fundoo.dto.request.ReminderRequest;
import com.bridgelabz.fundoo.dto.response.ReminderResponse;
import com.bridgelabz.fundoo.entity.CollaboratorRole;
import com.bridgelabz.fundoo.entity.Note;
import com.bridgelabz.fundoo.entity.Reminder;
import com.bridgelabz.fundoo.entity.User;
import com.bridgelabz.fundoo.exception.ResourceNotFoundException;
import com.bridgelabz.fundoo.mapper.FundooMapper;
import com.bridgelabz.fundoo.repository.NoteRepository;
import com.bridgelabz.fundoo.repository.ReminderRepository;
import com.bridgelabz.fundoo.service.interfaces.ReminderService;
import com.bridgelabz.fundoo.service.interfaces.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ReminderServiceImpl implements ReminderService {

    private static final Logger log = LoggerFactory.getLogger(ReminderServiceImpl.class);

    private final ReminderRepository reminderRepository;
    private final NoteRepository noteRepository;
    private final UserService userService;
    private final FundooMapper fundooMapper;

    public ReminderServiceImpl(ReminderRepository reminderRepository, NoteRepository noteRepository, UserService userService, FundooMapper fundooMapper) {
        this.reminderRepository = reminderRepository;
        this.noteRepository = noteRepository;
        this.userService = userService;
        this.fundooMapper = fundooMapper;
    }

    private void checkNoteWritePermission(Note note, User user) {
        if (note.getOwner().getId().equals(user.getId())) {
            return;
        }
        boolean isEditor = note.getCollaborators().stream()
                .anyMatch(c -> c.getUser().getId().equals(user.getId()) && c.getRole() == CollaboratorRole.EDITOR);
        if (!isEditor) {
            throw new AccessDeniedException("You do not have permission to modify reminders for this note.");
        }
    }

    private void checkNoteReadPermission(Note note, User user) {
        if (note.getOwner().getId().equals(user.getId())) {
            return;
        }
        boolean isCollaborator = note.getCollaborators().stream()
                .anyMatch(c -> c.getUser().getId().equals(user.getId()));
        if (!isCollaborator) {
            throw new AccessDeniedException("You do not have permission to access reminders for this note.");
        }
    }

    @Override
    @Transactional
    public ReminderResponse addReminder(Long noteId, ReminderRequest reminderRequest) {
        User currentUser = userService.getAuthenticatedUser();

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with ID: " + noteId));
        checkNoteWritePermission(note, currentUser);

        if (reminderRequest.getRemindAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reminder time cannot be in the past.");
        }

        Reminder reminder = Reminder.builder()
                .note(note)
                .remindAt(reminderRequest.getRemindAt())
                .notified(false)
                .build();

        Reminder saved = reminderRepository.save(reminder);
        log.info("Added reminder for note ID: {} set at {}", noteId, saved.getRemindAt());
        return fundooMapper.toReminderResponse(saved);
    }

    @Override
    public List<ReminderResponse> getRemindersOfNote(Long noteId) {
        User currentUser = userService.getAuthenticatedUser();

        Note note = noteRepository.findById(noteId)
                .orElseThrow(() -> new ResourceNotFoundException("Note not found with ID: " + noteId));
        checkNoteReadPermission(note, currentUser);

        List<Reminder> reminders = reminderRepository.findByNoteId(noteId);
        return reminders.stream().map(fundooMapper::toReminderResponse).collect(Collectors.toList());
    }

    @Override
    public ReminderResponse getReminderById(Long id) {
        User currentUser = userService.getAuthenticatedUser();
        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with ID: " + id));

        checkNoteReadPermission(reminder.getNote(), currentUser);
        return fundooMapper.toReminderResponse(reminder);
    }

    @Override
    @Transactional
    public ReminderResponse updateReminder(Long id, ReminderRequest reminderRequest) {
        User currentUser = userService.getAuthenticatedUser();
        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with ID: " + id));

        checkNoteWritePermission(reminder.getNote(), currentUser);

        if (reminderRequest.getRemindAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reminder time cannot be in the past.");
        }

        reminder.setRemindAt(reminderRequest.getRemindAt());
        reminder.setNotified(false); // Reset notified flag

        Reminder updated = reminderRepository.save(reminder);
        log.info("Updated reminder ID: {} set at {}", id, updated.getRemindAt());
        return fundooMapper.toReminderResponse(updated);
    }

    @Override
    @Transactional
    public void deleteReminder(Long id) {
        User currentUser = userService.getAuthenticatedUser();
        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with ID: " + id));

        checkNoteWritePermission(reminder.getNote(), currentUser);

        reminderRepository.delete(reminder);
        log.info("Deleted reminder with ID: {}", id);
    }

    @Override
    @Transactional
    public ReminderResponse snoozeReminder(Long id, Integer snoozeMinutes) {
        User currentUser = userService.getAuthenticatedUser();
        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reminder not found with ID: " + id));

        checkNoteWritePermission(reminder.getNote(), currentUser);

        int minutes = (snoozeMinutes == null) ? 15 : snoozeMinutes;
        reminder.setRemindAt(reminder.getRemindAt().plusMinutes(minutes));
        reminder.setNotified(false); // Reset notified flag so it triggers again

        Reminder updated = reminderRepository.save(reminder);
        log.info("Snoozed reminder ID: {} by {} minutes to {}", id, minutes, updated.getRemindAt());
        return fundooMapper.toReminderResponse(updated);
    }
}
