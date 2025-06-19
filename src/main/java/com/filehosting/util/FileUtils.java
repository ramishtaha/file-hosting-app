package com.filehosting.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FileUtils {
    
    private static final String[] ALLOWED_IMAGE_TYPES = {
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/bmp", "image/webp"
    };
    
    private static final String[] ALLOWED_VIDEO_TYPES = {
        "video/mp4", "video/avi", "video/mov", "video/wmv", "video/flv", "video/webm"
    };
    
    private static final String[] ALLOWED_DOCUMENT_TYPES = {
        "application/pdf", "text/plain", "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.ms-excel",
        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
    };
    
    public static boolean isValidFileType(MultipartFile file) {
        String contentType = file.getContentType();
        if (contentType == null) {
            return false;
        }
        
        return isImageType(contentType) || 
               isVideoType(contentType) || 
               isDocumentType(contentType) ||
               isArchiveType(contentType);
    }
    
    public static boolean isImageType(String contentType) {
        for (String type : ALLOWED_IMAGE_TYPES) {
            if (type.equals(contentType)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isVideoType(String contentType) {
        for (String type : ALLOWED_VIDEO_TYPES) {
            if (type.equals(contentType)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isDocumentType(String contentType) {
        for (String type : ALLOWED_DOCUMENT_TYPES) {
            if (type.equals(contentType)) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean isArchiveType(String contentType) {
        return contentType.equals("application/zip") ||
               contentType.equals("application/x-zip-compressed") ||
               contentType.equals("application/x-rar-compressed") ||
               contentType.equals("application/x-7z-compressed");
    }
    
    public static String formatFileSize(long bytes) {
        if (bytes <= 0) return "0 B";
        
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(bytes) / Math.log10(1024));
        
        return String.format("%.1f %s", 
            bytes / Math.pow(1024, digitGroups), 
            units[digitGroups]);
    }
    
    public static String generateTimestampedFilename(String originalFilename) {
        String timestamp = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss_SSS"));
        
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return "file_" + timestamp;
        }
        
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return originalFilename + "_" + timestamp;
        }
        
        String nameWithoutExtension = originalFilename.substring(0, lastDotIndex);
        String extension = originalFilename.substring(lastDotIndex);
        
        return nameWithoutExtension + "_" + timestamp + extension;
    }
    
    public static String calculateFileHash(Path filePath) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] fileBytes = Files.readAllBytes(filePath);
            byte[] hashBytes = md.digest(fileBytes);
            
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
            
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
    
    public static String sanitizeFilename(String filename) {
        if (filename == null) {
            return "unknown";
        }
        
        // Remove or replace unsafe characters
        return filename
            .replaceAll("[^a-zA-Z0-9._-]", "_")
            .replaceAll("_{2,}", "_")
            .trim();
    }
    
    public static boolean isFileSizeValid(long fileSize, long maxSizeBytes) {
        return fileSize > 0 && fileSize <= maxSizeBytes;
    }
    
    public static String getFileExtension(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        
        return filename.substring(lastDotIndex + 1).toLowerCase();
    }
    
    public static String getMimeTypeFromExtension(String extension) {
        if (extension == null) {
            return "application/octet-stream";
        }
        
        return switch (extension.toLowerCase()) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "bmp" -> "image/bmp";
            case "webp" -> "image/webp";
            case "mp4" -> "video/mp4";
            case "avi" -> "video/avi";
            case "mov" -> "video/mov";
            case "wmv" -> "video/wmv";
            case "flv" -> "video/flv";
            case "webm" -> "video/webm";
            case "pdf" -> "application/pdf";
            case "txt" -> "text/plain";
            case "doc" -> "application/msword";
            case "docx" -> "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls" -> "application/vnd.ms-excel";
            case "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "zip" -> "application/zip";
            case "rar" -> "application/x-rar-compressed";
            case "7z" -> "application/x-7z-compressed";
            default -> "application/octet-stream";
        };
    }
}
