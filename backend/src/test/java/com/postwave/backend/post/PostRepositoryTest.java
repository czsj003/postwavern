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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Testcontainers
class PostRepositoryTest {

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:17-alpine");

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void shouldSavePostWithAuthor() {
        User author = userRepository.saveAndFlush(
                new User(
                        "Justin",
                        "author@example.com",
                        "hashed-password"
                )
        );

        Post post = new Post(
                "My first post",
                "This is the content.",
                null,
                author
        );

        Post savedPost = postRepository.saveAndFlush(post);

        assertNotNull(savedPost.getId());
        assertNotNull(savedPost.getCreatedAt());
        assertNotNull(savedPost.getUpdatedAt());
        assertEquals("My first post", savedPost.getTitle());
        assertEquals(author.getId(), savedPost.getAuthor().getId());
    }

    @Test
    void shouldFindPostByIdAndAuthorId() {
        User author = userRepository.saveAndFlush(
                new User(
                        "Post Author",
                        "owner@example.com",
                        "hashed-password"
                )
        );

        Post post = postRepository.saveAndFlush(
                new Post(
                        "Owned post",
                        "Owned post content",
                        "example.png",
                        author
                )
        );

        Optional<Post> result =
                postRepository.findByIdAndAuthorId(
                        post.getId(),
                        author.getId()
                );

        assertTrue(result.isPresent());
        assertEquals("Owned post", result.get().getTitle());
        assertEquals("example.png", result.get().getImageName());
    }
}