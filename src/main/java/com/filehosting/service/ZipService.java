package com.filehosting.service;

import com.filehosting.model.UploadedFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class ZipService {
    
    @Autowired
    private FileStorageService fileStorageService;
    
    public File createZipFile(List<Long> fileIds, String zipFileName) throws IOException {
        // Create temporary zip file
        Path tempDir = Files.createTempDirectory("file_hosting_zip");
        Path zipPath = tempDir.resolve(zipFileName + ".zip");
        
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
            for (Long fileId : fileIds) {
                var fileOpt = fileStorageService.getFileById(fileId);
                if (fileOpt.isPresent()) {
                    UploadedFile uploadedFile = fileOpt.get();
                    addFileToZip(uploadedFile, zipOut);
                }
            }
        }
        
        return zipPath.toFile();
    }
    
    public File createZipFileFromAll() throws IOException {
        List<UploadedFile> allFiles = fileStorageService.getAllFiles();
        
        // Create temporary zip file
        Path tempDir = Files.createTempDirectory("file_hosting_zip");
        Path zipPath = tempDir.resolve("all_files.zip");
        
        try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipPath.toFile()))) {
            for (UploadedFile uploadedFile : allFiles) {
                addFileToZip(uploadedFile, zipOut);
            }
        }
        
        return zipPath.toFile();
    }
    
    private void addFileToZip(UploadedFile uploadedFile, ZipOutputStream zipOut) throws IOException {
        Path filePath = Paths.get(uploadedFile.getFilePath());
        
        if (Files.exists(filePath)) {
            // Use original filename in zip
            ZipEntry zipEntry = new ZipEntry(uploadedFile.getOriginalName());
            zipOut.putNextEntry(zipEntry);
            
            try (FileInputStream fis = new FileInputStream(filePath.toFile())) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fis.read(buffer)) >= 0) {
                    zipOut.write(buffer, 0, length);
                }
            }
            
            zipOut.closeEntry();
        }
    }
    
    public void cleanupTempFile(File tempFile) {
        if (tempFile != null && tempFile.exists()) {
            try {
                Files.deleteIfExists(tempFile.toPath());
                // Also try to delete parent temp directory if it's empty
                Path parentDir = tempFile.toPath().getParent();
                if (parentDir != null && Files.exists(parentDir)) {
                    try {
                        Files.deleteIfExists(parentDir);
                    } catch (IOException e) {
                        // Ignore - directory might not be empty
                    }
                }
            } catch (IOException e) {
                System.err.println("Failed to cleanup temp file: " + e.getMessage());
            }
        }
    }
}
