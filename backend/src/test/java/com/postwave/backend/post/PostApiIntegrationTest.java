package com.postwave.backend.post;

import com.postwave.backend.auth.JwtService;
import com.postwave.backend.user.User;
import com.postwave.backend.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class PostApiIntegrationTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17-alpine");

    @Autowired MockMvc mockMvc;
    @Autowired UserRepository userRepository;
    @Autowired PostRepository postRepository;
    @Autowired PostWaveRepository postWaveRepository;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired JwtService jwtService;

    private User owner;
    private User otherUser;
    private String ownerToken;
    private String otherToken;

    @BeforeEach
    void setUp() {
        postWaveRepository.deleteAll();
        postRepository.deleteAll();
        userRepository.deleteAll();

        owner = userRepository.save(new User(
                "Owner", "owner@example.com", passwordEncoder.encode("secret123")
        ));
        otherUser = userRepository.save(new User(
                "Other", "other@example.com", passwordEncoder.encode("secret123")
        ));
        ownerToken = tokenFor(owner);
        otherToken = tokenFor(otherUser);
    }

    @Test
    void shouldRequireAuthenticationForCreatingPost() throws Exception {
        mockMvc.perform(multipart("/api/posts")
                        .param("title", "A post")
                        .param("content", "Post content"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTHENTICATION_REQUIRED"));
    }

    @Test
    void shouldCompletePostAndWaveLifecycle() throws Exception {
        long postId = createPost(ownerToken);

        mockMvc.perform(get("/api/posts/{id}", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("First post"))
                .andExpect(jsonPath("$.userID").value(owner.getId()))
                .andExpect(jsonPath("$.waveCount").value(0));

        mockMvc.perform(patch("/api/posts/{id}/wave", postId)
                        .header("Authorization", bearer(otherToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.waved").value(true))
                .andExpect(jsonPath("$.count").value(1));

        mockMvc.perform(get("/api/posts/{id}/wavers", postId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("other@example.com"));

        MockMultipartHttpServletRequestBuilder update =
                MockMvcRequestBuilders.multipart("/api/posts/{id}", postId);
        update.with(request -> {
            request.setMethod("PUT");
            return request;
        });
        mockMvc.perform(update
                        .param("title", "Updated post")
                        .param("content", "Updated content")
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated post"));

        mockMvc.perform(delete("/api/posts/{id}", postId)
                        .header("Authorization", bearer(ownerToken)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/posts/{id}", postId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("POST_NOT_FOUND"));
    }

    @Test
    void shouldPreventAnotherUserFromEditingOrDeletingPost() throws Exception {
        long postId = createPost(ownerToken);

        MockMultipartHttpServletRequestBuilder update =
                MockMvcRequestBuilders.multipart("/api/posts/{id}", postId);
        update.with(request -> {
            request.setMethod("PUT");
            return request;
        });
        mockMvc.perform(update
                        .param("title", "Stolen")
                        .param("content", "Not allowed")
                        .header("Authorization", bearer(otherToken)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("POST_ACCESS_DENIED"));

        mockMvc.perform(delete("/api/posts/{id}", postId)
                        .header("Authorization", bearer(otherToken)))
                .andExpect(status().isForbidden());
    }

    private long createPost(String token) throws Exception {
        MvcResult result = mockMvc.perform(multipart("/api/posts")
                        .param("title", "First post")
                        .param("content", "Hello PostWave")
                        .header("Authorization", bearer(token)))
                .andExpect(status().isCreated())
                .andReturn();
        return Long.parseLong(result.getResponse().getContentAsString()
                .replaceAll(".*\\\"id\\\":(\\d+).*", "$1"));
    }

    private String tokenFor(User user) {
        return jwtService.generateToken(
                user.getId(), user.getEmail(), user.getName(), user.isAdmin()
        );
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}
