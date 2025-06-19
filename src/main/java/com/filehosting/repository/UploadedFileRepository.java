package com.filehosting.repository;

import com.filehosting.model.UploadedFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UploadedFileRepository extends JpaRepository<UploadedFile, Long> {
    
    Optional<UploadedFile> findByStoredName(String storedName);
    
    List<UploadedFile> findByOriginalNameContainingIgnoreCase(String name);
    
    List<UploadedFile> findByContentTypeStartingWith(String contentTypePrefix);
    
    List<UploadedFile> findByUploadTimeBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT SUM(f.fileSize) FROM UploadedFile f")
    Long getTotalUsedSpace();
    
    @Query("SELECT COUNT(f) FROM UploadedFile f")
    Long getTotalFileCount();
    
    @Query("SELECT f FROM UploadedFile f ORDER BY f.uploadTime DESC")
    List<UploadedFile> findAllOrderByUploadTimeDesc();
    
    @Query("SELECT f FROM UploadedFile f ORDER BY f.downloadCount DESC")
    List<UploadedFile> findAllOrderByDownloadCountDesc();
    
    @Query("SELECT f FROM UploadedFile f WHERE f.contentType LIKE 'image/%' ORDER BY f.uploadTime DESC")
    List<UploadedFile> findAllImages();
    
    @Query("SELECT f FROM UploadedFile f WHERE f.contentType LIKE 'video/%' ORDER BY f.uploadTime DESC")
    List<UploadedFile> findAllVideos();
}
