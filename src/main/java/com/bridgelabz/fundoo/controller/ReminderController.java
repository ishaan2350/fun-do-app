package com.bridgelabz.fundoo.controller;

import com.bridgelabz.fundoo.dto.common.ApiResponse;
import com.bridgelabz.fundoo.dto.request.ReminderRequest;
import com.bridgelabz.fundoo.dto.response.ReminderResponse;
import com.bridgelabz.fundoo.service.interfaces.ReminderService;
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
@Tag(name = "Reminder Module", description = "Endpoints for scheduling, snoozing, and managing note alerts")
public class ReminderController {

    private static final Logger log = LoggerFactory.getLogger(ReminderController.class);
    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @PostMapping("/notes/{noteId}/reminders")
    @Operation(summary = "Add a reminder", description = "Schedules a new reminder timestamp for a note. Requires editing permissions.")
    public ResponseEntity<ApiResponse<ReminderResponse>> addReminder(
            @PathVariable("noteId") Long noteId,
            @Valid @RequestBody ReminderRequest reminderRequest) {
        
        log.info("Request received to add reminder to note ID {}", noteId);
        ReminderResponse response = reminderService.addReminder(noteId, reminderRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Reminder added successfully", response));
    }

    @GetMapping("/notes/{noteId}/reminders")
    @Operation(summary = "Get note reminders", description = "Retrieves all reminders associated with a given note.")
    public ResponseEntity<ApiResponse<List<ReminderResponse>>> getRemindersOfNote(@PathVariable("noteId") Long noteId) {
        log.info("Request received to get reminders of note ID: {}", noteId);
        List<ReminderResponse> response = reminderService.getRemindersOfNote(noteId);
        return ResponseEntity.ok(ApiResponse.success("Reminders retrieved successfully", response));
    }

    @GetMapping("/reminders/{id}")
    @Operation(summary = "Get reminder by ID", description = "Retrieves specific reminder details by ID.")
    public ResponseEntity<ApiResponse<ReminderResponse>> getReminderById(@PathVariable("id") Long id) {
        log.info("Request received to get reminder ID: {}", id);
        ReminderResponse response = reminderService.getReminderById(id);
        return ResponseEntity.ok(ApiResponse.success("Reminder retrieved successfully", response));
    }

    @PutMapping("/reminders/{id}")
    @Operation(summary = "Update reminder time", description = "Updates the scheduled time for an existing reminder.")
    public ResponseEntity<ApiResponse<ReminderResponse>> updateReminder(
            @PathVariable("id") Long id,
            @Valid @RequestBody ReminderRequest reminderRequest) {
        
        log.info("Request received to update reminder ID: {}", id);
        ReminderResponse response = reminderService.updateReminder(id, reminderRequest);
        return ResponseEntity.ok(ApiResponse.success("Reminder updated successfully", response));
    }

    @DeleteMapping("/reminders/{id}")
    @Operation(summary = "Delete reminder", description = "Removes a scheduled reminder.")
    public ResponseEntity<ApiResponse<Void>> deleteReminder(@PathVariable("id") Long id) {
        log.info("Request received to delete reminder ID: {}", id);
        reminderService.deleteReminder(id);
        return ResponseEntity.ok(ApiResponse.success("Reminder deleted successfully"));
    }

    @PatchMapping("/reminders/{id}/snooze")
    @Operation(summary = "Snooze reminder", description = "Snoozes the reminder, shifting the remind time forward by a specified number of minutes (default 15).")
    public ResponseEntity<ApiResponse<ReminderResponse>> snoozeReminder(
            @PathVariable("id") Long id,
            @RequestParam(value = "snoozeMinutes", required = false) Integer snoozeMinutes) {
        
        log.info("Request received to snooze reminder ID: {} by {} minutes", id, snoozeMinutes);
        ReminderResponse response = reminderService.snoozeReminder(id, snoozeMinutes);
        return ResponseEntity.ok(ApiResponse.success("Reminder snoozed successfully", response));
    }
}
