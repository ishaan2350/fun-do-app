package com.bridgelabz.fundoo.controller;

import com.bridgelabz.fundoo.dto.request.LoginRequest;
import com.bridgelabz.fundoo.dto.request.RegisterRequest;
import com.bridgelabz.fundoo.dto.response.LoginResponse;
import com.bridgelabz.fundoo.dto.response.UserResponse;
import com.bridgelabz.fundoo.service.interfaces.UserService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Bypass security filters to focus on controller logic
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private UserDetailsService userDetailsService;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_ShouldReturnCreated_WhenPayloadIsValid() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("Password@123")
                .build();

        UserResponse responseDto = UserResponse.builder()
                .id(1L)
                .email("john.doe@example.com")
                .firstName("John")
                .lastName("Doe")
                .build();

        when(userService.register(any(RegisterRequest.class))).thenReturn(responseDto);

        mockMvc.perform(post("/api/v1/users/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("User registered successfully. Please verify your email."))
                .andExpect(jsonPath("$.data.email").value("john.doe@example.com"));
    }

    @Test
    void verifyEmail_ShouldReturnOk_WhenTokenIsPassed() throws Exception {
        mockMvc.perform(get("/api/v1/users/verify-email")
                        .param("token", "someToken"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Email verified and account activated successfully."));
    }

    @Test
    void login_ShouldReturnOk_WhenCredentialsAreValid() throws Exception {
        LoginRequest request = new LoginRequest("john.doe@example.com", "Password@123");
        LoginResponse response = LoginResponse.builder()
                .token("jwt-token")
                .user(UserResponse.builder().email("john.doe@example.com").build())
                .build();

        when(userService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/users/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.token").value("jwt-token"))
                .andExpect(jsonPath("$.data.user.email").value("john.doe@example.com"));
    }
}
