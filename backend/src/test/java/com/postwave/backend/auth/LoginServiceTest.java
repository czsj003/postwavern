package com.postwave.backend.auth;

import com.postwave.backend.user.User;
import com.postwave.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtService jwtService;
    @InjectMocks private LoginService loginService;

    @Test
    void shouldLoginWithValidCredentials() {
        User user = new User("Justin", "justin@example.com", "encoded-password");
        when(userRepository.findByEmail("justin@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("secret123", "encoded-password")).thenReturn(true);
        when(jwtService.generateToken(user.getId(), user.getEmail(), user.getName(), user.isAdmin()))
                .thenReturn("signed-jwt");
        when(jwtService.getExpirationSeconds()).thenReturn(86400L);

        LoginResult result = loginService.login(" JUSTIN@EXAMPLE.COM ", "secret123");

        assertEquals("signed-jwt", result.token());
        assertEquals("Bearer", result.tokenType());
        assertEquals(86400L, result.expiresIn());
        assertEquals("justin@example.com", result.user().email());
    }

    @Test
    void shouldRejectUnknownEmail() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());
        assertThrows(InvalidCredentialsException.class,
                () -> loginService.login("missing@example.com", "secret123"));
        verify(passwordEncoder, never()).matches(any(), any());
        verify(jwtService, never()).generateToken(any(), any(), any(), anyBoolean());
    }

    @Test
    void shouldRejectIncorrectPassword() {
        User user = new User("Justin", "justin@example.com", "encoded-password");
        when(userRepository.findByEmail("justin@example.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> loginService.login("justin@example.com", "wrong-password"));
        verify(jwtService, never()).generateToken(any(), any(), any(), anyBoolean());
    }
}
