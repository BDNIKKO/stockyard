package com.stockyard.common;

import com.stockyard.config.AppProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;

/**
 * Stores product images on local disk and serves them from /uploads.
 * Swap this class for an Azure Blob implementation in production.
 */
@Service
public class ImageStorage {
    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/png", ".png", "image/jpeg", ".jpg", "image/webp", ".webp", "image/gif", ".gif");

    private final Path dir;
    private final String baseUrl;

    public ImageStorage(AppProperties props) {
        this.dir = Path.of(props.uploads().dir()).toAbsolutePath().normalize();
        this.baseUrl = props.uploads().publicBaseUrl().replaceAll("/+$", "");
    }

    public String store(MultipartFile file) {
        String ext = EXTENSIONS.get(file.getContentType());
        if (ext == null) throw ApiException.badRequest("Image must be PNG, JPEG, WebP or GIF");
        try {
            Files.createDirectories(dir);
            String name = UUID.randomUUID() + ext;
            file.transferTo(dir.resolve(name));
            return baseUrl + "/" + name;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Configuration
    static class ServeUploads implements WebMvcConfigurer {
        private final Path dir;

        ServeUploads(AppProperties props) {
            this.dir = Path.of(props.uploads().dir()).toAbsolutePath().normalize();
        }

        @Override
        public void addResourceHandlers(ResourceHandlerRegistry registry) {
            registry.addResourceHandler("/uploads/**").addResourceLocations(dir.toUri().toString());
        }
    }
}
