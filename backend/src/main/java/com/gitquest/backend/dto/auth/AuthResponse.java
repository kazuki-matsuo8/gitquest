package com.gitquest.backend.dto.auth;

import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String tokenType,
        UUID userId,
        String username,
        String email
) {
    public static AuthResponse of(String token, UUID userId, String username, String email) {
        return new AuthResponse(token, "Bearer", userId, username, email);
    }
}
