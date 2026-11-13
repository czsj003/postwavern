package com.postwave.backend.security;

public record AuthenticatedUser(
        Long id,
        String name,
        String email,
        boolean admin
) {
}
