package com.filehosting.controller;

import com.filehosting.model.StorageInfo;
import com.filehosting.model.UploadedFile;
import com.filehosting.service.FileStorageService;
import com.filehosting.service.StorageMonitoringService;
import com.filehosting.service.ZipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Controller
public class FileHostingController {
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private StorageMonitoringService storageService;
    
    @Autowired
    private ZipService zipService;
    
    @GetMapping("/")
    public String index(Model model) {
        List<UploadedFile> files = fileStorageService.getAllFiles();
        StorageInfo storageInfo = storageService.getStorageInfo();
        
        model.addAttribute("files", files);
        model.addAttribute("storageInfo", storageInfo);
        model.addAttribute("isStorageWarning", storageService.isStorageWarning());
        model.addAttribute("isStorageFull", storageService.isStorageFull());
        
        return "index";
    }
    
    @PostMapping("/upload")
    public String uploadFiles(@RequestParam("files") MultipartFile[] files,
                             RedirectAttributes redirectAttributes) {
        
        if (files.length == 0) {
            redirectAttributes.addFlashAttribute("error", "Please select at least one file to upload.");
            return "redirect:/";
        }
        
        int uploadedCount = 0;
        StringBuilder errors = new StringBuilder();
        
        for (MultipartFile file : files) {
            if (file.isEmpty()) {
                continue;
            }
            
            try {
                // Check if storage can accommodate the file
                if (!storageService.canUploadFile(file.getSize())) {
                    errors.append("File '").append(file.getOriginalFilename())
                          .append("' exceeds available storage space. ");
                    continue;
                }
                
                // Validate file type
                if (!fileStorageService.isValidFileType(file)) {
                    errors.append("File '").append(file.getOriginalFilename())
                          .append("' has unsupported file type. ");
                    continue;
                }
                
                fileStorageService.storeFile(file);
                uploadedCount++;
                
            } catch (IOException e) {
                errors.append("Failed to upload '").append(file.getOriginalFilename())
                      .append("': ").append(e.getMessage()).append(" ");
            } catch (IllegalStateException e) {
                errors.append(e.getMessage()).append(" ");
                break; // Stop processing if storage is full
            }
        }
        
        if (uploadedCount > 0) {
            redirectAttributes.addFlashAttribute("success", 
                uploadedCount + " file(s) uploaded successfully!");
        }
        
        if (errors.length() > 0) {
            redirectAttributes.addFlashAttribute("error", errors.toString());
        }
        
        return "redirect:/";
    }
    
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long id) {
        Optional<UploadedFile> fileOpt = fileStorageService.getFileById(id);
        
        if (fileOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        UploadedFile uploadedFile = fileOpt.get();
        Path filePath = Paths.get(uploadedFile.getFilePath());
        
        if (!Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }
        
        // Increment download count
        fileStorageService.incrementDownloadCount(uploadedFile);
        
        Resource resource = new FileSystemResource(filePath);
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, 
                       "attachment; filename=\"" + uploadedFile.getOriginalName() + "\"")
                .header(HttpHeaders.CONTENT_TYPE, uploadedFile.getContentType())
                .body(resource);
    }
    
    @PostMapping("/delete/{id}")
    public String deleteFile(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean deleted = fileStorageService.deleteFile(id);
        
        if (deleted) {
            redirectAttributes.addFlashAttribute("success", "File deleted successfully!");
        } else {
            redirectAttributes.addFlashAttribute("error", "Failed to delete file.");
        }
        
        return "redirect:/";
    }
    
    @PostMapping("/download-zip")
    public ResponseEntity<Resource> downloadSelectedFiles(@RequestParam("selectedFiles") List<Long> fileIds,
                                                         @RequestParam(value = "zipName", defaultValue = "files") String zipName) {
        
        if (fileIds.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        try {
            File zipFile = zipService.createZipFile(fileIds, zipName);
            Resource resource = new FileSystemResource(zipFile);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"" + zipFile.getName() + "\"")
                    .header(HttpHeaders.CONTENT_TYPE, "application/zip")
                    .body(resource);
                    
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/download-all")
    public ResponseEntity<Resource> downloadAllFiles() {
        try {
            File zipFile = zipService.createZipFileFromAll();
            Resource resource = new FileSystemResource(zipFile);
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, 
                           "attachment; filename=\"all_files.zip\"")
                    .header(HttpHeaders.CONTENT_TYPE, "application/zip")
                    .body(resource);
                    
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/api/storage")
    @ResponseBody
    public StorageInfo getStorageInfo() {
        return storageService.getStorageInfo();
    }
    
    @GetMapping("/search")
    public String searchFiles(@RequestParam("q") String query, Model model) {
        List<UploadedFile> files = fileStorageService.searchFiles(query);
        StorageInfo storageInfo = storageService.getStorageInfo();
        
        model.addAttribute("files", files);
        model.addAttribute("storageInfo", storageInfo);
        model.addAttribute("searchQuery", query);
        model.addAttribute("isStorageWarning", storageService.isStorageWarning());
        model.addAttribute("isStorageFull", storageService.isStorageFull());
        
        return "index";
    }
    
    @GetMapping("/filter/{type}")
    public String filterFiles(@PathVariable String type, Model model) {
        List<UploadedFile> files;
        
        switch (type.toLowerCase()) {
            case "images":
                files = fileStorageService.getImageFiles();
                break;
            case "videos":
                files = fileStorageService.getVideoFiles();
                break;
            default:
                files = fileStorageService.getAllFiles();
        }
        
        StorageInfo storageInfo = storageService.getStorageInfo();
        
        model.addAttribute("files", files);
        model.addAttribute("storageInfo", storageInfo);
        model.addAttribute("filterType", type);
        model.addAttribute("isStorageWarning", storageService.isStorageWarning());
        model.addAttribute("isStorageFull", storageService.isStorageFull());
        
        return "index";
    }
}
