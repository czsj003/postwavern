package com.postwave.backend.post;

import com.postwave.backend.user.User;
import com.postwave.backend.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Testcontainers
class PostWaveRepositoryTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostWaveRepository postWaveRepository;

    @Test
    void shouldWavePost() {
        User author = saveUser(
                "Author",
                "wave-author@example.com"
        );

        User wavingUser = saveUser(
                "Waving User",
                "waver@example.com"
        );

        Post post = postRepository.saveAndFlush(
                new Post(
                        "Waveable post",
                        "Post content",
                        null,
                        author
                )
        );

        PostWave wave = postWaveRepository.saveAndFlush(
                new PostWave(post, wavingUser)
        );

        assertNotNull(wave.getCreatedAt());

        assertTrue(
                postWaveRepository.existsByPost_IdAndUser_Id(
                        post.getId(),
                        wavingUser.getId()
                )
        );

        assertEquals(
                1,
                postWaveRepository.countByPost_Id(post.getId())
        );
    }

    @Test
    void shouldRemoveWaveFromPost() {
        User author = saveUser(
                "Second Author",
                "remove-author@example.com"
        );

        User wavingUser = saveUser(
                "Second Waver",
                "remove-waver@example.com"
        );

        Post post = postRepository.saveAndFlush(
                new Post(
                        "Post to unwave",
                        "Content",
                        null,
                        author
                )
        );

        postWaveRepository.saveAndFlush(
                new PostWave(post, wavingUser)
        );

        long deletedRows =
                postWaveRepository.deleteByPost_IdAndUser_Id(
                        post.getId(),
                        wavingUser.getId()
                );

        postWaveRepository.flush();

        assertEquals(1, deletedRows);

        assertFalse(
                postWaveRepository.existsByPost_IdAndUser_Id(
                        post.getId(),
                        wavingUser.getId()
                )
        );
    }

    private User saveUser(String name, String email) {
        return userRepository.saveAndFlush(
                new User(
                        name,
                        email,
                        "hashed-password"
                )
        );
    }
}