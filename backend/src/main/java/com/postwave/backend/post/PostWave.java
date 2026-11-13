package com.postwave.backend.post;

import com.postwave.backend.user.User;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "post_waves")
public class PostWave {

    @EmbeddedId
    private PostWaveId id;

    @MapsId("postId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @MapsId("userId")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected PostWave() {
    }

    public PostWave(Post post, User user) {
        this.post = Objects.requireNonNull(post);
        this.user = Objects.requireNonNull(user);

        Long postId = Objects.requireNonNull(
                post.getId(),
                "Post must be persisted before it can be waved"
        );

        Long userId = Objects.requireNonNull(
                user.getId(),
                "User must be persisted before waving a post"
        );

        this.id = new PostWaveId(postId, userId);
    }

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
    }

    public PostWaveId getId() {
        return id;
    }

    public Post getPost() {
        return post;
    }

    public User getUser() {
        return user;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}