package com.bridgelabz.fundoo.mapper;

import com.bridgelabz.fundoo.entity.*;
import com.bridgelabz.fundoo.dto.response.*;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class FundooMapper {

    public UserResponse toUserResponse(User user) {
        if (user == null) return null;
        return UserResponse.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .verified(user.isVerified())
                .profilePic(user.getProfilePic())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    public LabelResponse toLabelResponse(Label label) {
        if (label == null) return null;
        return LabelResponse.builder()
                .id(label.getId())
                .name(label.getName())
                .userId(label.getUser() != null ? label.getUser().getId() : null)
                .createdAt(label.getCreatedAt())
                .updatedAt(label.getUpdatedAt())
                .build();
    }

    public CollaboratorResponse toCollaboratorResponse(Collaborator collaborator) {
        if (collaborator == null) return null;
        return CollaboratorResponse.builder()
                .id(collaborator.getId())
                .noteId(collaborator.getNote() != null ? collaborator.getNote().getId() : null)
                .userId(collaborator.getUser() != null ? collaborator.getUser().getId() : null)
                .userEmail(collaborator.getUser() != null ? collaborator.getUser().getEmail() : null)
                .role(collaborator.getRole().name())
                .addedAt(collaborator.getAddedAt())
                .build();
    }

    public ReminderResponse toReminderResponse(Reminder reminder) {
        if (reminder == null) return null;
        return ReminderResponse.builder()
                .id(reminder.getId())
                .noteId(reminder.getNote() != null ? reminder.getNote().getId() : null)
                .remindAt(reminder.getRemindAt())
                .notified(reminder.isNotified())
                .createdAt(reminder.getCreatedAt())
                .build();
    }

    public NoteResponse toNoteResponse(Note note) {
        if (note == null) return null;

        Set<LabelResponse> labelResponses = note.getLabels() != null 
                ? note.getLabels().stream().map(this::toLabelResponse).collect(Collectors.toSet())
                : Collections.emptySet();

        List<CollaboratorResponse> collaboratorResponses = note.getCollaborators() != null
                ? note.getCollaborators().stream().map(this::toCollaboratorResponse).collect(Collectors.toList())
                : Collections.emptyList();

        List<ReminderResponse> reminderResponses = note.getReminders() != null
                ? note.getReminders().stream().map(this::toReminderResponse).collect(Collectors.toList())
                : Collections.emptyList();

        return NoteResponse.builder()
                .id(note.getId())
                .title(note.getTitle())
                .description(note.getDescription())
                .color(note.getColor())
                .pinned(note.isPinned())
                .archived(note.isArchived())
                .trashed(note.isTrashed())
                .ownerId(note.getOwner() != null ? note.getOwner().getId() : null)
                .labels(labelResponses)
                .collaborators(collaboratorResponses)
                .reminders(reminderResponses)
                .createdAt(note.getCreatedAt())
                .updatedAt(note.getUpdatedAt())
                .build();
    }
}
