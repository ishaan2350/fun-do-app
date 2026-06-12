package com.bridgelabz.fundoo.controller;

import com.bridgelabz.fundoo.dto.common.ApiResponse;
import com.bridgelabz.fundoo.dto.request.CollaboratorRequest;
import com.bridgelabz.fundoo.dto.response.CollaboratorResponse;
import com.bridgelabz.fundoo.service.interfaces.CollaboratorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "Collaborator Module", description = "Endpoints for sharing notes with collaborators (add, view, and remove collaborators)")
public class CollaboratorController {

    private static final Logger log = LoggerFactory.getLogger(CollaboratorController.class);
    private final CollaboratorService collaboratorService;

    public CollaboratorController(CollaboratorService collaboratorService) {
        this.collaboratorService = collaboratorService;
    }

    @PostMapping("/notes/{noteId}/collaborators")
    @Operation(summary = "Add a collaborator", description = "Shares the note with another user by email, setting their access role (EDITOR/VIEWER). Only the owner can do this.")
    public ResponseEntity<ApiResponse<CollaboratorResponse>> addCollaborator(
            @PathVariable("noteId") Long noteId,
            @Valid @RequestBody CollaboratorRequest collaboratorRequest) {
        
        log.info("Request received to add collaborator {} to note ID {}", collaboratorRequest.getEmail(), noteId);
        CollaboratorResponse response = collaboratorService.addCollaborator(noteId, collaboratorRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Collaborator added successfully", response));
    }

    @GetMapping("/notes/{noteId}/collaborators")
    @Operation(summary = "Get note collaborators", description = "Lists all collaborators who have access to the specified note.")
    public ResponseEntity<ApiResponse<List<CollaboratorResponse>>> getCollaboratorsOfNote(@PathVariable("noteId") Long noteId) {
        log.info("Request received to get collaborators of note ID: {}", noteId);
        List<CollaboratorResponse> response = collaboratorService.getCollaboratorsOfNote(noteId);
        return ResponseEntity.ok(ApiResponse.success("Collaborators retrieved successfully", response));
    }

    @GetMapping("/collaborators/{id}")
    @Operation(summary = "Get collaborator details", description = "Fetches a specific collaborator record by ID.")
    public ResponseEntity<ApiResponse<CollaboratorResponse>> getCollaboratorById(@PathVariable("id") Long id) {
        log.info("Request received to get collaborator record ID: {}", id);
        CollaboratorResponse response = collaboratorService.getCollaboratorById(id);
        return ResponseEntity.ok(ApiResponse.success("Collaborator record retrieved successfully", response));
    }

    @DeleteMapping("/notes/{noteId}/collaborators/{id}")
    @Operation(summary = "Remove a collaborator", description = "Removes access for a specific collaborator by their collaborator record ID. Only the owner can do this.")
    public ResponseEntity<ApiResponse<Void>> removeCollaborator(
            @PathVariable("noteId") Long noteId,
            @PathVariable("id") Long id) {
        
        log.info("Request received to remove collaborator ID {} from note ID {}", id, noteId);
        collaboratorService.removeCollaborator(noteId, id);
        return ResponseEntity.ok(ApiResponse.success("Collaborator removed successfully"));
    }
}
