package com.bridgelabz.fundoo.controller;

import com.bridgelabz.fundoo.dto.common.ApiResponse;
import com.bridgelabz.fundoo.dto.request.UpdateUserRequest;
import com.bridgelabz.fundoo.dto.response.UserResponse;
import com.bridgelabz.fundoo.service.interfaces.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@SecurityRequirement(name = "Bearer Authentication")
@Tag(name = "User Module", description = "Endpoints for managing user details (CRUD, pagination, search, and soft delete)")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "Get list of users", description = "Retrieves a paginated, sorted list of users, with optional email search filtering.")
    public ResponseEntity<ApiResponse<Page<UserResponse>>> getAllUsers(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "sortBy", defaultValue = "id") String sortBy,
            @RequestParam(value = "direction", defaultValue = "asc") String direction,
            @RequestParam(value = "emailSearch", required = false) String emailSearch) {
        
        log.info("Request received to fetch users list - page: {}, size: {}, sortBy: {}, emailSearch: {}", page, size, sortBy, emailSearch);
        Page<UserResponse> users = userService.getAllUsers(page, size, sortBy, direction, emailSearch);
        return ResponseEntity.ok(ApiResponse.success("Users retrieved successfully", users));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Fetches a specific user's details by their ID.")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable("id") Long id) {
        log.info("Request received to fetch user by ID: {}", id);
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User retrieved successfully", user));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update user details", description = "Updates a user's details like first name, last name, mobile number, and profile pic.")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateUserRequest updateUserRequest) {
        
        log.info("Request received to update user ID: {}", id);
        UserResponse updated = userService.updateUser(id, updateUserRequest);
        return ResponseEntity.ok(ApiResponse.success("User details updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete user", description = "Marks the user as deleted, making them inactive without purging them from database.")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable("id") Long id) {
        log.info("Request received to delete user ID: {}", id);
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("User soft-deleted successfully"));
    }
}
