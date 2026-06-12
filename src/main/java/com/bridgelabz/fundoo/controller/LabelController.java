package com.bridgelabz.fundoo.controller;

import com.bridgelabz.fundoo.dto.common.ApiResponse;
import com.bridgelabz.fundoo.dto.request.LabelRequest;
import com.bridgelabz.fundoo.dto.response.LabelResponse;
import com.bridgelabz.fundoo.dto.response.NoteResponse;
import com.bridgelabz.fundoo.service.interfaces.LabelService;
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

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Label Module", description = "Endpoints for managing labels and attaching/detaching labels to notes")
public class LabelController {

    private static final Logger log = LoggerFactory.getLogger(LabelController.class);
    private final LabelService labelService;

    public LabelController(LabelService labelService) {
        this.labelService = labelService;
    }

    @PostMapping("/labels")
    @Operation(summary = "Create a label", description = "Creates a new custom label for note categorisation.")
    public ResponseEntity<ApiResponse<LabelResponse>> createLabel(@Valid @RequestBody LabelRequest labelRequest) {
        log.info("Request received to create label: {}", labelRequest.getName());
        LabelResponse response = labelService.createLabel(labelRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Label created successfully", response));
    }

    @GetMapping("/labels")
    @Operation(summary = "Get user labels", description = "Lists all labels created by the authenticated user.")
    public ResponseEntity<ApiResponse<List<LabelResponse>>> getAllLabels() {
        log.info("Request received to get all labels");
        List<LabelResponse> response = labelService.getAllLabels();
        return ResponseEntity.ok(ApiResponse.success("Labels retrieved successfully", response));
    }

    @GetMapping("/labels/{id}")
    @Operation(summary = "Get label by ID", description = "Fetches details of a specific label by its ID.")
    public ResponseEntity<ApiResponse<LabelResponse>> getLabelById(@PathVariable("id") Long id) {
        log.info("Request received to get label ID: {}", id);
        LabelResponse response = labelService.getLabelById(id);
        return ResponseEntity.ok(ApiResponse.success("Label retrieved successfully", response));
    }

    @PutMapping("/labels/{id}")
    @Operation(summary = "Update label name", description = "Updates the name of a specific label, checking ownership.")
    public ResponseEntity<ApiResponse<LabelResponse>> updateLabel(
            @PathVariable("id") Long id,
            @Valid @RequestBody LabelRequest labelRequest) {
        
        log.info("Request received to update label ID: {}", id);
        LabelResponse response = labelService.updateLabel(id, labelRequest);
        return ResponseEntity.ok(ApiResponse.success("Label updated successfully", response));
    }

    @DeleteMapping("/labels/{id}")
    @Operation(summary = "Delete label", description = "Deletes a label and automatically clears it from any notes.")
    public ResponseEntity<ApiResponse<Void>> deleteLabel(@PathVariable("id") Long id) {
        log.info("Request received to delete label ID: {}", id);
        labelService.deleteLabel(id);
        return ResponseEntity.ok(ApiResponse.success("Label deleted successfully"));
    }

    @PostMapping("/notes/{noteId}/labels/{labelId}")
    @Operation(summary = "Attach label to note", description = "Associates a label with a specific note.")
    public ResponseEntity<ApiResponse<NoteResponse>> attachLabel(
            @PathVariable("noteId") Long noteId,
            @PathVariable("labelId") Long labelId) {
        
        log.info("Request received to attach label ID {} to note ID {}", labelId, noteId);
        NoteResponse response = labelService.attachLabel(noteId, labelId);
        return ResponseEntity.ok(ApiResponse.success("Label attached to note successfully", response));
    }

    @DeleteMapping("/notes/{noteId}/labels/{labelId}")
    @Operation(summary = "Detach label from note", description = "Removes the association between a label and a note.")
    public ResponseEntity<ApiResponse<NoteResponse>> detachLabel(
            @PathVariable("noteId") Long noteId,
            @PathVariable("labelId") Long labelId) {
        
        log.info("Request received to detach label ID {} from note ID {}", labelId, noteId);
        NoteResponse response = labelService.detachLabel(noteId, labelId);
        return ResponseEntity.ok(ApiResponse.success("Label detached from note successfully", response));
    }

    @GetMapping("/labels/{labelId}/notes")
    @Operation(summary = "Get notes under label", description = "Retrieves all notes associated with a given label, with pagination and sorting.")
    public ResponseEntity<ApiResponse<Page<NoteResponse>>> getNotesByLabel(
            @PathVariable("labelId") Long labelId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "direction", defaultValue = "desc") String direction) {
        
        log.info("Request received to get notes under label ID: {}", labelId);
        Page<NoteResponse> notes = labelService.getNotesByLabel(labelId, page, size, sortBy, direction);
        return ResponseEntity.ok(ApiResponse.success("Notes retrieved successfully", notes));
    }
}
