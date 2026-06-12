package com.bridgelabz.fundoo.service.interfaces;

import com.bridgelabz.fundoo.dto.request.NoteRequest;
import com.bridgelabz.fundoo.dto.response.NoteResponse;
import org.springframework.data.domain.Page;

public interface NoteService {
    NoteResponse createNote(NoteRequest noteRequest);
    NoteResponse getNoteById(Long id);
    NoteResponse updateNote(Long id, NoteRequest noteRequest);
    void deleteNote(Long id); // Trashes the note; if already trashed, permanently deletes it.
    
    NoteResponse togglePin(Long id);
    NoteResponse toggleArchive(Long id);
    NoteResponse toggleTrash(Long id);
    NoteResponse updateColor(Long id, String color);
    
    Page<NoteResponse> getAllNotes(int page, int size, String sortBy, String direction, Boolean pinned, Boolean archived, Boolean trashed);
    Page<NoteResponse> searchNotes(String query, int page, int size, String sortBy, String direction);
    
    void purgeOldTrashedNotes();
}
