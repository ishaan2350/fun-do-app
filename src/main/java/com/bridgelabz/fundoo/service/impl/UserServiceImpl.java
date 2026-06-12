package com.bridgelabz.fundoo.service.impl;

import com.bridgelabz.fundoo.dto.request.*;
import com.bridgelabz.fundoo.dto.response.LoginResponse;
import com.bridgelabz.fundoo.dto.response.UserResponse;
import com.bridgelabz.fundoo.entity.User;
import com.bridgelabz.fundoo.exception.*;
import com.bridgelabz.fundoo.mapper.FundooMapper;
import com.bridgelabz.fundoo.repository.UserRepository;
import com.bridgelabz.fundoo.service.interfaces.EmailService;
import com.bridgelabz.fundoo.service.interfaces.UserService;
import com.bridgelabz.fundoo.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final FundooMapper fundooMapper;
    private final AuthenticationManager authenticationManager;

    public UserServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, EmailService emailService, FundooMapper fundooMapper, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
        this.fundooMapper = fundooMapper;
        this.authenticationManager = authenticationManager;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new UserAlreadyExistsException("A user is already registered with email: " + registerRequest.getEmail());
        }

        User user = User.builder()
                .firstName(registerRequest.getFirstName())
                .lastName(registerRequest.getLastName())
                .email(registerRequest.getEmail())
                .password(passwordEncoder.encode(registerRequest.getPassword()))
                .mobileNumber(registerRequest.getMobileNumber())
                .verified(false)
                .deleted(false)
                .build();

        User savedUser = userRepository.save(user);

        // Generate verification token and send email
        String verificationToken = jwtUtil.generateVerificationToken(savedUser.getEmail());
        emailService.sendVerificationEmail(savedUser.getEmail(), verificationToken);

        log.info("Registered user with email: {}. Verification email sent.", savedUser.getEmail());
        return fundooMapper.toUserResponse(savedUser);
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        if (!jwtUtil.validateVerificationToken(token)) {
            throw new InvalidTokenException("The verification token is invalid or expired.");
        }

        String email = jwtUtil.extractEmail(token);
        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (user.isVerified()) {
            log.info("User {} is already verified.", email);
            return;
        }

        user.setVerified(true);
        userRepository.save(user);
        log.info("User email verified successfully: {}", email);
    }

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        User user = userRepository.findByEmailAndDeletedFalse(loginRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + loginRequest.getEmail()));

        if (!user.isVerified()) {
            throw new AuthenticationException("Your email address is not verified yet. Please check your inbox to verify.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword())
            );
        } catch (org.springframework.security.core.AuthenticationException e) {
            throw new AuthenticationException("Invalid email or password.");
        }

        String jwt = jwtUtil.generateToken(user.getEmail());
        log.info("[DEVELOPER MODE] Login JWT token for {}: {}", user.getEmail(), jwt);
        log.info("User logged in successfully: {}", user.getEmail());

        return LoginResponse.builder()
                .token(jwt)
                .user(fundooMapper.toUserResponse(user))
                .build();
    }

    @Override
    @Transactional
    public void forgotPassword(ForgotPasswordRequest forgotPasswordRequest) {
        User user = userRepository.findByEmailAndDeletedFalse(forgotPasswordRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + forgotPasswordRequest.getEmail()));

        String resetToken = jwtUtil.generateResetToken(user.getEmail());
        emailService.sendForgotPasswordEmail(user.getEmail(), resetToken);
        log.info("Password reset token generated and sent to email: {}", user.getEmail());
    }

    @Override
    @Transactional
    public void resetPassword(ResetPasswordRequest resetPasswordRequest) {
        if (!jwtUtil.validateResetToken(resetPasswordRequest.getToken())) {
            throw new InvalidTokenException("The password reset token is invalid or expired.");
        }

        String email = jwtUtil.extractEmail(resetPasswordRequest.getToken());
        User user = userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        user.setPassword(passwordEncoder.encode(resetPasswordRequest.getNewPassword()));
        userRepository.save(user);
        log.info("Password successfully reset for user: {}", email);
    }

    @Override
    public Page<UserResponse> getAllUsers(int page, int size, String sortBy, String direction, String emailSearch) {
        Sort.Direction sortDirection = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(sortDirection, sortBy));

        Page<User> users;
        if (emailSearch != null && !emailSearch.trim().isEmpty()) {
            users = userRepository.findByEmailContainingIgnoreCaseAndDeletedFalse(emailSearch, pageable);
        } else {
            users = userRepository.findByDeletedFalse(pageable);
        }

        return users.map(fundooMapper::toUserResponse);
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));
        return fundooMapper.toUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long id, UpdateUserRequest updateUserRequest) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        user.setFirstName(updateUserRequest.getFirstName());
        user.setLastName(updateUserRequest.getLastName());
        if (updateUserRequest.getMobileNumber() != null) {
            user.setMobileNumber(updateUserRequest.getMobileNumber());
        }
        if (updateUserRequest.getProfilePic() != null) {
            user.setProfilePic(updateUserRequest.getProfilePic());
        }

        User updatedUser = userRepository.save(user);
        log.info("User details updated successfully for ID: {}", id);
        return fundooMapper.toUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + id));

        user.setDeleted(true);
        userRepository.save(user);
        log.info("User soft-deleted successfully for ID: {}", id);
    }

    @Override
    public User getAuthenticatedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String email;
        if (principal instanceof UserDetails) {
            email = ((UserDetails) principal).getUsername();
        } else {
            email = principal.toString();
        }

        return userRepository.findByEmailAndDeletedFalse(email)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found or has been deleted."));
    }
}
