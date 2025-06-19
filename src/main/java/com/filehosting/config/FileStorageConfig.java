package com.filehosting.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import jakarta.annotation.PostConstruct;
import java.io.File;

@Configuration
public class FileStorageConfig implements WebMvcConfigurer {
    
    @Value("${app.upload.path}")
    private String uploadPath;
    
    @Value("${app.upload.max-size-gb}")
    private int maxStorageGb;
    
    @PostConstruct
    public void init() {
        // Create upload directory if it doesn't exist
        File uploadDir = new File(uploadPath);
        if (!uploadDir.exists()) {
            boolean created = uploadDir.mkdirs();
            if (created) {
                System.out.println("Created upload directory: " + uploadPath);
            } else {
                System.err.println("Failed to create upload directory: " + uploadPath);
            }
        }
    }
    
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Serve uploaded files
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
        
        // Serve static resources
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
    
    public String getUploadPath() {
        return uploadPath;
    }
    
    public long getMaxStorageBytes() {
        return (long) maxStorageGb * 1024 * 1024 * 1024; // Convert GB to bytes
    }
    
    public int getMaxStorageGb() {
        return maxStorageGb;
    }
}
