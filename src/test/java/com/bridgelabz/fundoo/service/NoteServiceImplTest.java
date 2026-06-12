package com.bridgelabz.fundoo.service;

import com.bridgelabz.fundoo.dto.request.NoteRequest;
import com.bridgelabz.fundoo.dto.response.NoteResponse;
import com.bridgelabz.fundoo.entity.Note;
import com.bridgelabz.fundoo.entity.User;
import com.bridgelabz.fundoo.exception.ResourceNotFoundException;
import com.bridgelabz.fundoo.mapper.FundooMapper;
import com.bridgelabz.fundoo.repository.NoteRepository;
import com.bridgelabz.fundoo.service.impl.NoteServiceImpl;
import com.bridgelabz.fundoo.service.interfaces.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NoteServiceImplTest {

    @Mock
    private NoteRepository noteRepository;

    @Mock
    private UserService userService;

    @Mock
    private FundooMapper fundooMapper;

    @InjectMocks
    private NoteServiceImpl noteService;

    private User currentUser;
    private User otherUser;
    private Note note;
    private NoteRequest noteRequest;

    @BeforeEach
    void setUp() {
        currentUser = User.builder().id(1L).email("owner@example.com").build();
        otherUser = User.builder().id(2L).email("other@example.com").build();
        
        note = Note.builder()
                .id(10L)
                .title("Test Note")
                .description("Content")
                .color("white")
                .pinned(false)
                .archived(false)
                .trashed(false)
                .owner(currentUser)
                .build();

        noteRequest = NoteRequest.builder()
                .title("Updated Title")
                .description("Updated Content")
                .color("yellow")
                .build();
    }

    @Test
    void createNote_ShouldSaveAndReturnNote() {
        when(userService.getAuthenticatedUser()).thenReturn(currentUser);
        when(noteRepository.save(any(Note.class))).thenReturn(note);
        
        NoteResponse responseDto = NoteResponse.builder()
                .id(10L)
                .title(note.getTitle())
                .ownerId(1L)
                .build();
        when(fundooMapper.toNoteResponse(note)).thenReturn(responseDto);

        NoteResponse result = noteService.createNote(noteRequest);

        assertNotNull(result);
        assertEquals("Test Note", result.getTitle());
        verify(noteRepository, times(1)).save(any(Note.class));
    }

    @Test
    void getNoteById_ShouldReturnNote_WhenUserIsOwner() {
        when(userService.getAuthenticatedUser()).thenReturn(currentUser);
        when(noteRepository.findById(10L)).thenReturn(Optional.of(note));
        
        NoteResponse responseDto = NoteResponse.builder().id(10L).title(note.getTitle()).build();
        when(fundooMapper.toNoteResponse(note)).thenReturn(responseDto);

        NoteResponse result = noteService.getNoteById(10L);

        assertNotNull(result);
        assertEquals(10L, result.getId());
    }

    @Test
    void getNoteById_ShouldThrowAccessDenied_WhenUserIsNotOwnerOrCollaborator() {
        when(userService.getAuthenticatedUser()).thenReturn(otherUser);
        when(noteRepository.findById(10L)).thenReturn(Optional.of(note));

        assertThrows(AccessDeniedException.class, () -> noteService.getNoteById(10L));
    }

    @Test
    void deleteNote_ShouldTrashNote_WhenNoteIsNotAlreadyTrashed() {
        when(userService.getAuthenticatedUser()).thenReturn(currentUser);
        when(noteRepository.findById(10L)).thenReturn(Optional.of(note));

        noteService.deleteNote(10L);

        assertTrue(note.isTrashed());
        assertFalse(note.isPinned());
        assertFalse(note.isArchived());
        verify(noteRepository, times(1)).save(note);
        verify(noteRepository, never()).delete(any(Note.class));
    }

    @Test
    void deleteNote_ShouldPermanentlyDeleteNote_WhenNoteIsAlreadyTrashed() {
        note.setTrashed(true);
        when(userService.getAuthenticatedUser()).thenReturn(currentUser);
        when(noteRepository.findById(10L)).thenReturn(Optional.of(note));

        noteService.deleteNote(10L);

        verify(noteRepository, times(1)).delete(note);
        verify(noteRepository, never()).save(any(Note.class));
    }
}
