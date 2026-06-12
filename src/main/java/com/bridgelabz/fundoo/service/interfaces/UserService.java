package com.bridgelabz.fundoo.service.interfaces;

import com.bridgelabz.fundoo.dto.request.*;
import com.bridgelabz.fundoo.dto.response.LoginResponse;
import com.bridgelabz.fundoo.dto.response.UserResponse;
import com.bridgelabz.fundoo.entity.User;
import org.springframework.data.domain.Page;

public interface UserService {
    UserResponse register(RegisterRequest registerRequest);
    void verifyEmail(String token);
    LoginResponse login(LoginRequest loginRequest);
    void forgotPassword(ForgotPasswordRequest forgotPasswordRequest);
    void resetPassword(ResetPasswordRequest resetPasswordRequest);
    
    Page<UserResponse> getAllUsers(int page, int size, String sortBy, String direction, String emailSearch);
    UserResponse getUserById(Long id);
    UserResponse updateUser(Long id, UpdateUserRequest updateUserRequest);
    void deleteUser(Long id);

    User getAuthenticatedUser();
}
