package com.postwave.backend.post;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class ImageStorageService {

    private static final Set<String> ALLOWED_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );
    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/gif", ".gif",
            "image/webp", ".webp"
    );

    private final Path uploadDirectory;

    public ImageStorageService(@Value("${app.upload.directory}") String uploadDirectory) {
        this.uploadDirectory = Path.of(uploadDirectory).toAbsolutePath().normalize();
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        String contentType = file.getContentType() == null
                ? "" : file.getContentType().toLowerCase(Locale.ROOT);
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new ImageStorageException("Only JPEG, PNG, GIF, and WebP images are allowed");
        }

        String fileName = UUID.randomUUID() + EXTENSIONS.get(contentType);
        Path destination = uploadDirectory.resolve(fileName).normalize();
        if (!destination.getParent().equals(uploadDirectory)) {
            throw new ImageStorageException("Invalid upload path");
        }

        try {
            Files.createDirectories(uploadDirectory);
            Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);
            return fileName;
        } catch (IOException exception) {
            throw new ImageStorageException("Could not store image", exception);
        }
    }

    public void delete(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return;
        }
        Path target = uploadDirectory.resolve(fileName).normalize();
        if (!target.getParent().equals(uploadDirectory)) {
            return;
        }
        try {
            Files.deleteIfExists(target);
        } catch (IOException exception) {
            throw new ImageStorageException("Could not delete image", exception);
        }
    }

    public Path getUploadDirectory() {
        return uploadDirectory;
    }
}
