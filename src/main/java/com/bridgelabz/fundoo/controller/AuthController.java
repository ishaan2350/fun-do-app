package com.bridgelabz.fundoo.controller;

import com.bridgelabz.fundoo.dto.common.ApiResponse;
import com.bridgelabz.fundoo.dto.request.*;
import com.bridgelabz.fundoo.dto.response.LoginResponse;
import com.bridgelabz.fundoo.dto.response.UserResponse;
import com.bridgelabz.fundoo.service.interfaces.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Authentication Module", description = "Endpoints for user registration, login, email verification, and password resets")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Validates details, encrypts password, saves the user, and sends verification email.")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        log.info("Request received to register user: {}", registerRequest.getEmail());
        UserResponse response = userService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully. Please verify your email.", response));
    }

    @GetMapping("/verify-email")
    @Operation(summary = "Verify user email token", description = "Verifies the activation token and activates the user account.")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@RequestParam("token") String token) {
        log.info("Request received to verify email token");
        userService.verifyEmail(token);
        return ResponseEntity.ok(ApiResponse.success("Email verified and account activated successfully."));
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates user and returns JWT access token along with user profile.")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest loginRequest) {
        log.info("Request received for user login: {}", loginRequest.getEmail());
        LoginResponse response = userService.login(loginRequest);
        return ResponseEntity.ok(ApiResponse.success("Login successful", response));
    }

    @PostMapping("/forgot-password")
    @Operation(summary = "Forgot password request", description = "Generates a reset token and emails it to the user.")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody ForgotPasswordRequest forgotPasswordRequest) {
        log.info("Request received to send reset password link for: {}", forgotPasswordRequest.getEmail());
        userService.forgotPassword(forgotPasswordRequest);
        return ResponseEntity.ok(ApiResponse.success("Reset password link has been sent to your email."));
    }

    @PostMapping("/reset-password")
    @Operation(summary = "Reset password request", description = "Validates the reset token and updates the user's password.")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        log.info("Request received to reset password");
        userService.resetPassword(resetPasswordRequest);
        return ResponseEntity.ok(ApiResponse.success("Password has been reset successfully."));
    }
}
