package com.bridgelabz.fundoo.controller;

import com.bridgelabz.fundoo.dto.common.ApiResponse;
import com.bridgelabz.fundoo.dto.request.NoteRequest;
import com.bridgelabz.fundoo.dto.response.NoteResponse;
import com.bridgelabz.fundoo.service.interfaces.NoteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/notes")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Note Module", description = "Endpoints for managing notes (CRUD, pin, archive, trash, color updates, and search)")
public class NoteController {

    private static final Logger log = LoggerFactory.getLogger(NoteController.class);
    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @PostMapping
    @Operation(summary = "Create a note", description = "Creates a new note associated with the authenticated user.")
    public ResponseEntity<ApiResponse<NoteResponse>> createNote(@Valid @RequestBody NoteRequest noteRequest) {
        log.info("Request received to create a note");
        NoteResponse note = noteService.createNote(noteRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Note created successfully", note));
    }

    @GetMapping
    @Operation(summary = "Get list of notes", description = "Retrieves a paginated list of notes, with optional filtering for pinned, archived, or trashed states.")
    public ResponseEntity<ApiResponse<Page<NoteResponse>>> getAllNotes(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "direction", defaultValue = "desc") String direction,
            @RequestParam(value = "pinned", required = false) Boolean pinned,
            @RequestParam(value = "archived", defaultValue = "false") Boolean archived,
            @RequestParam(value = "trashed", defaultValue = "false") Boolean trashed) {
        
        log.info("Request received to fetch notes page: {}, size: {}, pinned: {}, archived: {}, trashed: {}", page, size, pinned, archived, trashed);
        Page<NoteResponse> notes = noteService.getAllNotes(page, size, sortBy, direction, pinned, archived, trashed);
        return ResponseEntity.ok(ApiResponse.success("Notes retrieved successfully", notes));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get note by ID", description = "Retrieves a specific note by its ID, checking read permissions.")
    public ResponseEntity<ApiResponse<NoteResponse>> getNoteById(@PathVariable("id") Long id) {
        log.info("Request received to fetch note ID: {}", id);
        NoteResponse note = noteService.getNoteById(id);
        return ResponseEntity.ok(ApiResponse.success("Note retrieved successfully", note));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update note", description = "Updates note details, verifying editing permissions (user must be owner or EDITOR collaborator).")
    public ResponseEntity<ApiResponse<NoteResponse>> updateNote(
            @PathVariable("id") Long id,
            @Valid @RequestBody NoteRequest noteRequest) {
        
        log.info("Request received to update note ID: {}", id);
        NoteResponse note = noteService.updateNote(id, noteRequest);
        return ResponseEntity.ok(ApiResponse.success("Note updated successfully", note));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete note", description = "Moves the note to trash; if already trashed, deletes the note permanently.")
    public ResponseEntity<ApiResponse<Void>> deleteNote(@PathVariable("id") Long id) {
        log.info("Request received to delete note ID: {}", id);
        noteService.deleteNote(id);
        return ResponseEntity.ok(ApiResponse.success("Note deleted/trashed successfully"));
    }

    @PatchMapping("/{id}/pin")
    @Operation(summary = "Toggle pin status", description = "Toggles the pinned status of a note. Pinned notes cannot be archived.")
    public ResponseEntity<ApiResponse<NoteResponse>> togglePin(@PathVariable("id") Long id) {
        log.info("Request received to toggle pin status of note ID: {}", id);
        NoteResponse note = noteService.togglePin(id);
        return ResponseEntity.ok(ApiResponse.success("Note pin status toggled successfully", note));
    }

    @PatchMapping("/{id}/archive")
    @Operation(summary = "Toggle archive status", description = "Toggles the archived status of a note. Archived notes cannot be pinned.")
    public ResponseEntity<ApiResponse<NoteResponse>> toggleArchive(@PathVariable("id") Long id) {
        log.info("Request received to toggle archive status of note ID: {}", id);
        NoteResponse note = noteService.toggleArchive(id);
        return ResponseEntity.ok(ApiResponse.success("Note archive status toggled successfully", note));
    }

    @PatchMapping("/{id}/trash")
    @Operation(summary = "Toggle trash status", description = "Toggles the trashed status of a note. Trashed notes cannot be pinned or archived.")
    public ResponseEntity<ApiResponse<NoteResponse>> toggleTrash(@PathVariable("id") Long id) {
        log.info("Request received to toggle trash status of note ID: {}", id);
        NoteResponse note = noteService.toggleTrash(id);
        return ResponseEntity.ok(ApiResponse.success("Note trash status toggled successfully", note));
    }

    @PatchMapping("/{id}/color")
    @Operation(summary = "Update note color", description = "Changes the background color of the note. Must be a supported color.")
    public ResponseEntity<ApiResponse<NoteResponse>> updateColor(
            @PathVariable("id") Long id,
            @RequestParam("color") String color) {
        
        log.info("Request received to update color of note ID: {} to {}", id, color);
        NoteResponse note = noteService.updateColor(id, color);
        return ResponseEntity.ok(ApiResponse.success("Note color updated successfully", note));
    }

    @GetMapping("/search")
    @Operation(summary = "Search notes", description = "Searches for notes containing the query in their title or description using specifications.")
    public ResponseEntity<ApiResponse<Page<NoteResponse>>> searchNotes(
            @RequestParam("query") String query,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "direction", defaultValue = "desc") String direction) {
        
        log.info("Request received to search notes for query: {}", query);
        Page<NoteResponse> notes = noteService.searchNotes(query, page, size, sortBy, direction);
        return ResponseEntity.ok(ApiResponse.success("Notes searched successfully", notes));
    }
}
