package com.postwave.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final String uploadDirectory;

    public WebConfig(@Value("${app.upload.directory:uploads}") String uploadDirectory) {
        this.uploadDirectory = uploadDirectory;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(
                        java.nio.file.Path.of(uploadDirectory).toAbsolutePath().normalize().toUri().toString()
                );
    }
}
