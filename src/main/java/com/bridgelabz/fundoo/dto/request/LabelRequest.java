package com.bridgelabz.fundoo.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LabelRequest {

    @NotBlank(message = "Label name is required")
    @Size(max = 100, message = "Label name must not exceed 100 characters")
    private String name;

    // Constructors
    public LabelRequest() {
    }

    public LabelRequest(String name) {
        this.name = name;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
