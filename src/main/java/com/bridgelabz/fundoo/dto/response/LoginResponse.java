package com.bridgelabz.fundoo.dto.response;

public class LoginResponse {
    private String token;
    private UserResponse user;

    // Constructors
    public LoginResponse() {
    }

    public LoginResponse(String token, UserResponse user) {
        this.token = token;
        this.user = user;
    }

    // Getters and Setters
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public UserResponse getUser() { return user; }
    public void setUser(UserResponse user) { this.user = user; }

    // Manual Builder
    public static LoginResponseBuilder builder() {
        return new LoginResponseBuilder();
    }

    public static class LoginResponseBuilder {
        private String token;
        private UserResponse user;

        public LoginResponseBuilder token(String token) { this.token = token; return this; }
        public LoginResponseBuilder user(UserResponse user) { this.user = user; return this; }

        public LoginResponse build() {
            return new LoginResponse(token, user);
        }
    }
}
