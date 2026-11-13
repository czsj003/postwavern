package com.postwave.backend.post;

import com.postwave.backend.security.AuthenticatedUser;
import com.postwave.backend.user.User;
import com.postwave.backend.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final PostWaveRepository postWaveRepository;
    private final UserRepository userRepository;
    private final ImageStorageService imageStorageService;

    public PostService(
            PostRepository postRepository,
            PostWaveRepository postWaveRepository,
            UserRepository userRepository,
            ImageStorageService imageStorageService
    ) {
        this.postRepository = postRepository;
        this.postWaveRepository = postWaveRepository;
        this.userRepository = userRepository;
        this.imageStorageService = imageStorageService;
    }

    @Transactional(readOnly = true)
    public List<PostResponse> findAll(String filter, Long currentUserId) {
        List<PostResponse> posts = postRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(post -> toResponse(post, currentUserId))
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
        if ("popular".equalsIgnoreCase(filter)) {
            posts.sort(Comparator.comparingLong(PostResponse::waveCount).reversed()
                    .thenComparing(PostResponse::createdAt, Comparator.reverseOrder()));
        }
        return posts;
    }

    @Transactional(readOnly = true)
    public PostResponse findById(Long postId, Long currentUserId) {
        return toResponse(requirePost(postId), currentUserId);
    }

    @Transactional
    public PostResponse create(
            String title,
            String content,
            MultipartFile image,
            AuthenticatedUser principal
    ) {
        User author = userRepository.getReferenceById(principal.id());
        String imageName = imageStorageService.store(image);
        Post post = new Post(title.trim(), content.trim(), imageName, author);
        return toResponse(postRepository.save(post), principal.id());
    }

    @Transactional
    public PostResponse update(
            Long postId,
            String title,
            String content,
            MultipartFile image,
            AuthenticatedUser principal
    ) {
        Post post = requirePost(postId);
        verifyOwnerOrAdmin(post, principal);

        String oldImageName = post.getImageName();
        String imageName = oldImageName;
        if (image != null && !image.isEmpty()) {
            imageName = imageStorageService.store(image);
        }
        post.update(title.trim(), content.trim(), imageName);
        PostResponse response = toResponse(post, principal.id());

        if (imageName != null && !imageName.equals(oldImageName)) {
            imageStorageService.delete(oldImageName);
        }
        return response;
    }

    @Transactional
    public void delete(Long postId, AuthenticatedUser principal) {
        Post post = requirePost(postId);
        verifyOwnerOrAdmin(post, principal);
        String imageName = post.getImageName();
        postRepository.delete(post);
        postRepository.flush();
        imageStorageService.delete(imageName);
    }

    @Transactional
    public WaveResponse toggleWave(Long postId, AuthenticatedUser principal) {
        Post post = requirePost(postId);
        boolean alreadyWaved = postWaveRepository
                .existsByPost_IdAndUser_Id(postId, principal.id());

        boolean waved;
        if (alreadyWaved) {
            postWaveRepository.deleteByPost_IdAndUser_Id(postId, principal.id());
            waved = false;
        } else {
            User user = userRepository.getReferenceById(principal.id());
            postWaveRepository.save(new PostWave(post, user));
            waved = true;
        }

        long count = postWaveRepository.countByPost_Id(postId);
        return new WaveResponse(waved ? "Waved!" : "Unwaved!", count, waved);
    }

    @Transactional(readOnly = true)
    public List<WaverResponse> findWavers(Long postId) {
        requirePost(postId);
        return postWaveRepository.findAllByPost_IdOrderByCreatedAtAsc(postId).stream()
                .map(wave -> new WaverResponse(
                        wave.getUser().getId(),
                        wave.getUser().getName(),
                        wave.getUser().getEmail()
                ))
                .toList();
    }

    private Post requirePost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
    }

    private void verifyOwnerOrAdmin(Post post, AuthenticatedUser principal) {
        if (!principal.admin() && !post.getAuthor().getId().equals(principal.id())) {
            throw new PostAccessDeniedException();
        }
    }

    private PostResponse toResponse(Post post, Long currentUserId) {
        List<Long> waverIds = postWaveRepository
                .findAllByPost_IdOrderByCreatedAtAsc(post.getId()).stream()
                .map(wave -> wave.getUser().getId())
                .toList();
        return new PostResponse(
                post.getId(), post.getTitle(), post.getContent(), post.getImageName(),
                post.getAuthor().getId(), post.getAuthor().getName(), waverIds,
                waverIds.size(), currentUserId != null && waverIds.contains(currentUserId),
                post.getCreatedAt(), post.getUpdatedAt()
        );
    }
}
