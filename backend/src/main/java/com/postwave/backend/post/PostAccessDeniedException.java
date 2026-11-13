package com.postwave.backend.post;

public class PostAccessDeniedException extends RuntimeException {
    public PostAccessDeniedException() {
        super("You are not allowed to modify this post");
    }
}
