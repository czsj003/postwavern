package com.postwave.backend.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
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
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistrationService registrationService;

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
}