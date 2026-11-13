package com.postwave.backend.auth;

public record LoginResult(
        String token,
        String tokenType,
        long expiresIn,
        RegisteredUser user
) {
}
