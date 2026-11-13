package com.postwave.backend.auth;

import com.postwave.backend.user.User;
import com.postwave.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegistrationService registrationService;

    @Test
    void shouldNormalizeAndRegisterUser() {
        when(userRepository.existsByEmail("justin@example.com"))
                .thenReturn(false);

        when(passwordEncoder.encode("secret123"))
                .thenReturn("encoded-password");

        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RegisteredUser result = registrationService.register(
                "  Justin  ",
                "  JUSTIN@EXAMPLE.COM  ",
                "secret123"
        );

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals("Justin", savedUser.getName());
        assertEquals("justin@example.com", savedUser.getEmail());
        assertEquals(
                "encoded-password",
                savedUser.getPasswordHash()
        );

        assertEquals("Justin", result.name());
        assertEquals("justin@example.com", result.email());

        verify(passwordEncoder).encode("secret123");
    }

    @Test
    void shouldRejectDuplicateEmail() {
        when(userRepository.existsByEmail("used@example.com"))
                .thenReturn(true);

        assertThrows(
                DuplicateEmailException.class,
                () -> registrationService.register(
                        "Existing User",
                        "USED@EXAMPLE.COM",
                        "secret123"
                )
        );

        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }
}