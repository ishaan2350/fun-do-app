package com.bridgelabz.fundoo.service;

import com.bridgelabz.fundoo.dto.request.LoginRequest;
import com.bridgelabz.fundoo.dto.request.RegisterRequest;
import com.bridgelabz.fundoo.dto.response.LoginResponse;
import com.bridgelabz.fundoo.dto.response.UserResponse;
import com.bridgelabz.fundoo.entity.User;
import com.bridgelabz.fundoo.exception.AuthenticationException;
import com.bridgelabz.fundoo.exception.ResourceNotFoundException;
import com.bridgelabz.fundoo.exception.UserAlreadyExistsException;
import com.bridgelabz.fundoo.mapper.FundooMapper;
import com.bridgelabz.fundoo.repository.UserRepository;
import com.bridgelabz.fundoo.service.impl.UserServiceImpl;
import com.bridgelabz.fundoo.service.interfaces.EmailService;
import com.bridgelabz.fundoo.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private EmailService emailService;

    @Mock
    private FundooMapper fundooMapper;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L)
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("encodedPassword")
                .mobileNumber("1234567890")
                .verified(false)
                .deleted(false)
                .build();

        registerRequest = RegisterRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.doe@example.com")
                .password("Password@123")
                .mobileNumber("1234567890")
                .build();
    }

    @Test
    void register_ShouldSaveUserAndSendEmail_WhenEmailIsUnique() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(jwtUtil.generateVerificationToken(anyString())).thenReturn("verificationToken");
        
        UserResponse responseDto = UserResponse.builder()
                .id(1L)
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .build();
        when(fundooMapper.toUserResponse(user)).thenReturn(responseDto);

        UserResponse result = userService.register(registerRequest);

        assertNotNull(result);
        assertEquals(user.getEmail(), result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService, times(1)).sendVerificationEmail(eq(user.getEmail()), eq("verificationToken"));
    }

    @Test
    void register_ShouldThrowException_WhenUserAlreadyExists() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> userService.register(registerRequest));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void verifyEmail_ShouldActivateAccount_WhenTokenIsValid() {
        String token = "validToken";
        when(jwtUtil.validateVerificationToken(token)).thenReturn(true);
        when(jwtUtil.extractEmail(token)).thenReturn("john.doe@example.com");
        when(userRepository.findByEmailAndDeletedFalse("john.doe@example.com")).thenReturn(Optional.of(user));

        userService.verifyEmail(token);

        assertTrue(user.isVerified());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void login_ShouldReturnToken_WhenCredentialsAreValidAndVerified() {
        user.setVerified(true);
        LoginRequest loginRequest = new LoginRequest("john.doe@example.com", "Password@123");
        
        when(userRepository.findByEmailAndDeletedFalse(loginRequest.getEmail())).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(user.getEmail())).thenReturn("jwtToken");
        
        UserResponse userResponse = UserResponse.builder().email(user.getEmail()).build();
        when(fundooMapper.toUserResponse(user)).thenReturn(userResponse);

        LoginResponse result = userService.login(loginRequest);

        assertNotNull(result);
        assertEquals("jwtToken", result.getToken());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void login_ShouldThrowException_WhenUserIsNotVerified() {
        LoginRequest loginRequest = new LoginRequest("john.doe@example.com", "Password@123");
        when(userRepository.findByEmailAndDeletedFalse(loginRequest.getEmail())).thenReturn(Optional.of(user));

        assertThrows(AuthenticationException.class, () -> userService.login(loginRequest));
        verify(authenticationManager, never()).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
