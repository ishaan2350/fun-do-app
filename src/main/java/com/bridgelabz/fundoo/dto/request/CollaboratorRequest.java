package com.bridgelabz.fundoo.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class CollaboratorRequest {

    @NotBlank(message = "Collaborator email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    private String role = "VIEWER";

    // Constructors
    public CollaboratorRequest() {
    }

    public CollaboratorRequest(String email, String role) {
        this.email = email;
        this.role = role;
    }

    // Getters and Setters
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
