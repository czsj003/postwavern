package com.postwave.backend.auth;

import com.postwave.backend.security.JwtAuthenticationFilter;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrationService registrationService;

    @MockitoBean
    private LoginService loginService;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    void shouldRegisterUser() throws Exception {
        when(registrationService.register(
                "Justin",
                "justin@example.com",
                "secret123"
        )).thenReturn(
                new RegisteredUser(
                        1L,
                        "Justin",
                        "justin@example.com"
                )
        );

        mockMvc.perform(
                        post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "Justin",
                                          "email": "justin@example.com",
                                          "password": "secret123"
                                        }
                                        """)
                )
                .andExpect(status().isCreated())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Justin"))
                .andExpect(
                        jsonPath("$.email")
                                .value("justin@example.com")
                );

        verify(registrationService).register(
                "Justin",
                "justin@example.com",
                "secret123"
        );
    }

    @Test
    void shouldReturnConflictForDuplicateEmail()
            throws Exception {
        when(registrationService.register(
                "Justin",
                "used@example.com",
                "secret123"
        )).thenThrow(
                new DuplicateEmailException(
                        "used@example.com"
                )
        );

        mockMvc.perform(
                        post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "Justin",
                                          "email": "used@example.com",
                                          "password": "secret123"
                                        }
                                        """)
                )
                .andExpect(status().isConflict())
                .andExpect(
                        jsonPath("$.code")
                                .value("EMAIL_ALREADY_REGISTERED")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value(
                                        "Email is already registered: "
                                                + "used@example.com"
                                )
                );
    }

    @Test
    void shouldReturnBadRequestForInvalidInput()
            throws Exception {
        mockMvc.perform(
                        post("/api/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                        {
                                          "name": "",
                                          "email": "not-an-email",
                                          "password": "short"
                                        }
                                        """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value("VALIDATION_FAILED")
                )
                .andExpect(
                        jsonPath("$.fieldErrors.name")
                                .value("Name is required")
                )
                .andExpect(
                        jsonPath("$.fieldErrors.email")
                                .value("Email must be valid")
                )
                .andExpect(
                        jsonPath("$.fieldErrors.password")
                                .value(
                                        "Password must contain between "
                                                + "8 and 72 characters"
                                )
                );

        verifyNoInteractions(registrationService);
    }

    @Test
    void shouldLoginUser() throws Exception {
        LoginResult loginResult = new LoginResult(
                "signed-jwt-token",
                "Bearer",
                86400,
                new RegisteredUser(
                        1L,
                        "Justin",
                        "justin@example.com"
                )
        );

        when(loginService.login(
                "justin@example.com",
                "secret123"
        )).thenReturn(loginResult);

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "email": "justin@example.com",
                                      "password": "secret123"
                                    }
                                    """)
                )
                .andExpect(status().isOk())
                .andExpect(
                        content().contentTypeCompatibleWith(
                                MediaType.APPLICATION_JSON
                        )
                )
                .andExpect(
                        jsonPath("$.token")
                                .value("signed-jwt-token")
                )
                .andExpect(
                        jsonPath("$.tokenType")
                                .value("Bearer")
                )
                .andExpect(
                        jsonPath("$.expiresIn")
                                .value(86400)
                )
                .andExpect(
                        jsonPath("$.user.id")
                                .value(1)
                )
                .andExpect(
                        jsonPath("$.user.name")
                                .value("Justin")
                )
                .andExpect(
                        jsonPath("$.user.email")
                                .value("justin@example.com")
                );

        verify(loginService).login(
                "justin@example.com",
                "secret123"
        );
    }

    @Test
    void shouldReturnUnauthorizedForInvalidCredentials()
            throws Exception {
        when(loginService.login(
                "justin@example.com",
                "wrong-password"
        )).thenThrow(new InvalidCredentialsException());

        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "email": "justin@example.com",
                                      "password": "wrong-password"
                                    }
                                    """)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(
                        jsonPath("$.code")
                                .value("INVALID_CREDENTIALS")
                )
                .andExpect(
                        jsonPath("$.message")
                                .value("Invalid email or password")
                );
    }

    @Test
    void shouldRejectInvalidLoginRequest()
            throws Exception {
        mockMvc.perform(
                        post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                                    {
                                      "email": "not-an-email",
                                      "password": ""
                                    }
                                    """)
                )
                .andExpect(status().isBadRequest())
                .andExpect(
                        jsonPath("$.code")
                                .value("VALIDATION_FAILED")
                )
                .andExpect(
                        jsonPath("$.fieldErrors.email")
                                .value("Email must be valid")
                )
                .andExpect(
                        jsonPath("$.fieldErrors.password")
                                .value("Password is required")
                );

        verifyNoInteractions(loginService);
    }
}
