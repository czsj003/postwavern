package com.postwave.backend.auth;

public record RegisteredUser(
        Long id,
        String name,
        String email
) {
}