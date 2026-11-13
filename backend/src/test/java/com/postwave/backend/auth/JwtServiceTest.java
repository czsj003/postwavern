package com.postwave.backend.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {
    private static final String SECRET =
            "cG9zdHdhdmUtdGVzdC1qd3Qtc2VjcmV0LW11c3QtYmUtYXQtbGVhc3QtMzItYnl0ZXM=";
    private final JwtService jwtService = new JwtService(SECRET, 3600);

    @Test
    void shouldGenerateAndVerifyToken() {
        String token = jwtService.generateToken(
                42L, "justin@example.com", "Justin", false
        );
        assertTrue(jwtService.isValid(token));
        assertEquals(42L, jwtService.extractUserId(token));
        assertEquals(3600, jwtService.getExpirationSeconds());
    }

    @Test
    void shouldRejectTamperedToken() {
        String token = jwtService.generateToken(
                42L, "justin@example.com", "Justin", false
        );
        String tamperedToken = token.substring(0, token.length() - 1) + "x";
        assertFalse(jwtService.isValid(tamperedToken));
    }
}
