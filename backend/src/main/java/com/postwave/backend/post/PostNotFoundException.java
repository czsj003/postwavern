package com.postwave.backend.post;

public class PostNotFoundException extends RuntimeException {
    public PostNotFoundException(Long postId) {
        super("Post not found: " + postId);
    }
}
