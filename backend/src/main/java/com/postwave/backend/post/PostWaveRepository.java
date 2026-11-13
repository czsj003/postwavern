package com.postwave.backend.post;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostWaveRepository
        extends JpaRepository<PostWave, PostWaveId> {

    boolean existsByPost_IdAndUser_Id(Long postId, Long userId);

    long countByPost_Id(Long postId);

    long deleteByPost_IdAndUser_Id(Long postId, Long userId);

    List<PostWave> findAllByPost_IdOrderByCreatedAtAsc(Long postId);
}
