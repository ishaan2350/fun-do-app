package com.bridgelabz.fundoo.controller;

import com.bridgelabz.fundoo.dto.request.NoteRequest;
import com.bridgelabz.fundoo.dto.response.NoteResponse;
import com.bridgelabz.fundoo.service.interfaces.NoteService;
import com.bridgelabz.fundoo.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NoteController.class)
@AutoConfigureMockMvc(addFilters = false) // Bypass security filters to focus on controller logic
public class NoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NoteService noteService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createNote_ShouldReturnCreated_WhenPayloadIsValid() throws Exception {
        NoteRequest request = NoteRequest.builder()
                .title("New Note")
                .description("Note description")
                .color("white")
                .build();

        NoteResponse response = NoteResponse.builder()
                .id(1L)
                .title("New Note")
                .description("Note description")
                .color("white")
                .build();

        when(noteService.createNote(any(NoteRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/notes")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.title").value("New Note"));
    }

    @Test
    void getNoteById_ShouldReturnOk_WhenNoteExists() throws Exception {
        NoteResponse response = NoteResponse.builder()
                .id(1L)
                .title("My Note")
                .build();

        when(noteService.getNoteById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/notes/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(1L))
                .andExpect(jsonPath("$.data.title").value("My Note"));
    }

    @Test
    void deleteNote_ShouldReturnOk_WhenNoteIsDeleted() throws Exception {
        mockMvc.perform(delete("/api/v1/notes/1")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Note deleted/trashed successfully"));
    }
}
