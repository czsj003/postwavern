package com.postwave.backend.auth;

import com.postwave.backend.user.User;
import com.postwave.backend.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class RegistrationService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisteredUser register(
            String name,
            String email,
            String rawPassword
    ) {
        String normalizedName = name.trim();
        String normalizedEmail =
                email.trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException(normalizedEmail);
        }

        String passwordHash =
                passwordEncoder.encode(rawPassword);

        User user = new User(
                normalizedName,
                normalizedEmail,
                passwordHash
        );

        User savedUser = userRepository.save(user);

        return new RegisteredUser(
                savedUser.getId(),
                savedUser.getName(),
                savedUser.getEmail()
        );
    }
}