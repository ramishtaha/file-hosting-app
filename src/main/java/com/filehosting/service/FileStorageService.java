package com.filehosting.service;

import com.filehosting.config.FileStorageConfig;
import com.filehosting.model.UploadedFile;
import com.filehosting.repository.UploadedFileRepository;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileStorageService {
    
    @Autowired
    private UploadedFileRepository fileRepository;
    
    @Autowired
    private FileStorageConfig fileConfig;
      public UploadedFile storeFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Cannot store empty file");
        }
        
        // Validate file size (20GB = 21,474,836,480 bytes)
        long maxFileSize = 21474836480L; // 20GB in bytes
        if (file.getSize() > maxFileSize) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 20GB");
        }
        
        // Check storage limits
        long currentUsedSpace = getCurrentUsedSpace();
        long maxStorage = fileConfig.getMaxStorageBytes();
        
        if (currentUsedSpace + file.getSize() > maxStorage) {
            throw new IllegalStateException("Storage limit exceeded. Cannot upload file.");
        }
        
        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null) {
            originalFileName = "unknown";
        }
        
        // Generate unique stored filename
        String storedFileName = generateUniqueFileName(originalFileName);
        
        // Create full path
        Path uploadPath = Paths.get(fileConfig.getUploadPath());
        Path filePath = uploadPath.resolve(storedFileName);
        
        // Ensure upload directory exists
        Files.createDirectories(uploadPath);
        
        // Copy file to destination
        Files.copy(file.getInputStream(), filePath);
        
        // Create database record
        UploadedFile uploadedFile = new UploadedFile(
            originalFileName,
            storedFileName,
            filePath.toString(),
            file.getSize(),
            file.getContentType()
        );
        
        return fileRepository.save(uploadedFile);
    }
    
    public List<UploadedFile> getAllFiles() {
        return fileRepository.findAllOrderByUploadTimeDesc();
    }
    
    public Optional<UploadedFile> getFileById(Long id) {
        return fileRepository.findById(id);
    }
    
    public Optional<UploadedFile> getFileByStoredName(String storedName) {
        return fileRepository.findByStoredName(storedName);
    }
    
    public boolean deleteFile(Long id) {
        Optional<UploadedFile> fileOpt = fileRepository.findById(id);
        if (fileOpt.isPresent()) {
            UploadedFile uploadedFile = fileOpt.get();
            
            // Delete physical file
            try {
                Path filePath = Paths.get(uploadedFile.getFilePath());
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // Log the error but continue with database deletion
                System.err.println("Failed to delete physical file: " + e.getMessage());
            }
            
            // Delete database record
            fileRepository.delete(uploadedFile);
            return true;
        }
        return false;
    }
    
    public void incrementDownloadCount(UploadedFile file) {
        file.incrementDownloadCount();
        fileRepository.save(file);
    }
    
    public long getCurrentUsedSpace() {
        Long usedSpace = fileRepository.getTotalUsedSpace();
        return usedSpace != null ? usedSpace : 0L;
    }
    
    public long getFileCount() {
        Long count = fileRepository.getTotalFileCount();
        return count != null ? count : 0L;
    }
    
    public List<UploadedFile> getImageFiles() {
        return fileRepository.findAllImages();
    }
    
    public List<UploadedFile> getVideoFiles() {
        return fileRepository.findAllVideos();
    }
    
    public List<UploadedFile> searchFiles(String query) {
        return fileRepository.findByOriginalNameContainingIgnoreCase(query);
    }
    
    private String generateUniqueFileName(String originalFileName) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String extension = FilenameUtils.getExtension(originalFileName);
        
        if (extension.isEmpty()) {
            return String.format("%s_%s", timestamp, uuid);
        } else {
            return String.format("%s_%s.%s", timestamp, uuid, extension);
        }
    }
    
    public boolean isValidFileType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }
        
        // Allow common file types
        return contentType.startsWith("image/") ||
               contentType.startsWith("video/") ||
               contentType.startsWith("audio/") ||
               contentType.equals("application/pdf") ||
               contentType.startsWith("text/") ||
               contentType.equals("application/zip") ||
               contentType.equals("application/x-zip-compressed");
    }
}
