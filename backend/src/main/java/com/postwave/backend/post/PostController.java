package com.postwave.backend.post;

import com.postwave.backend.security.AuthenticatedUser;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping
    public List<PostResponse> findAll(
            @RequestParam(required = false) String filter,
            Authentication authentication
    ) {
        return postService.findAll(filter, currentUserId(authentication));
    }

    @GetMapping("/{id}")
    public PostResponse findById(@PathVariable Long id, Authentication authentication) {
        return postService.findById(id, currentUserId(authentication));
    }

    @PostMapping(consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponse create(
            @RequestParam @NotBlank @Size(max = 200) String title,
            @RequestParam @NotBlank String content,
            @RequestParam(required = false) MultipartFile image,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        return postService.create(title, content, image, principal);
    }

    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public PostResponse update(
            @PathVariable Long id,
            @RequestParam @NotBlank @Size(max = 200) String title,
            @RequestParam @NotBlank String content,
            @RequestParam(required = false) MultipartFile image,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        return postService.update(id, title, content, image, principal);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        postService.delete(id, principal);
    }

    @PatchMapping("/{id}/wave")
    public WaveResponse toggleWave(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser principal
    ) {
        return postService.toggleWave(id, principal);
    }

    @GetMapping("/{id}/wavers")
    public List<WaverResponse> findWavers(@PathVariable Long id) {
        return postService.findWavers(id);
    }

    private Long currentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof AuthenticatedUser user)) {
            return null;
        }
        return user.id();
    }
}
