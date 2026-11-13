package com.postwave.backend.post;

import java.time.Instant;
import java.util.List;

public record PostResponse(
        Long id,
        String title,
        String content,
        String imageName,
        Long userID,
        String authorName,
        List<Long> listOfUsersWaved,
        long waveCount,
        boolean wavedByCurrentUser,
        Instant createdAt,
        Instant updatedAt
) {
}
