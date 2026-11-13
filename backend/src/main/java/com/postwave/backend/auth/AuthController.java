package com.postwave.backend.auth;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegistrationService registrationService;
    private final LoginService loginService;

    public AuthController(
            RegistrationService registrationService,
            LoginService loginService
    ) {
        this.registrationService = registrationService;
        this.loginService = loginService;
    }

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public RegisteredUser register(
            @Valid @RequestBody RegisterRequest request
    ) {
        return registrationService.register(
                request.name(),
                request.email(),
                request.password()
        );
    }

    @PostMapping("/login")
    public LoginResult login(
            @Valid @RequestBody LoginRequest request
    ) {
        return loginService.login(
                request.email(),
                request.password()
        );
    }
}