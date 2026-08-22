package edu.demart_api.dto.response;

import lombok.Getter;

@Getter
public class AuthResponse {

    private final String accessToken;
    private final String tokenType = "Bearer";
    private final Long userId;
    private final String name;
    private final String email;
    private final String role;

    public AuthResponse(String accessToken, Long userId, String name, String email, String role) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.name = name;
        this.email = email;
        this.role = role;
    }
}
