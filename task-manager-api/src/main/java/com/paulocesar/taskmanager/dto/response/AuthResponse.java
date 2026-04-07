package com.paulocesar.taskmanager.dto.response;

import com.paulocesar.taskmanager.domain.enums.UserRole;

public record AuthResponse(
        String token,
        String type,
        Long userId,
        String name,
        String email,
        UserRole role
) {
    public static AuthResponse of(String token, Long userId, String name, String email, UserRole role) {
        return new AuthResponse(token, "Bearer", userId, name, email, role);
    }
}
