package com.filehosting.service;

import com.filehosting.config.FileStorageConfig;
import com.filehosting.model.StorageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StorageMonitoringService {
    
    @Autowired
    private FileStorageConfig fileConfig;
    
    @Autowired
    private FileStorageService fileStorageService;
    
    public StorageInfo getStorageInfo() {
        long maxStorage = fileConfig.getMaxStorageBytes();
        long usedSpace = fileStorageService.getCurrentUsedSpace();
        long fileCount = fileStorageService.getFileCount();
        
        return new StorageInfo(maxStorage, usedSpace, fileCount);
    }
    
    public boolean isStorageFull() {
        StorageInfo info = getStorageInfo();
        return info.getUsagePercentage() >= 95.0;
    }
    
    public boolean isStorageWarning() {
        StorageInfo info = getStorageInfo();
        return info.getUsagePercentage() >= 80.0;
    }
    
    public boolean canUploadFile(long fileSize) {
        StorageInfo info = getStorageInfo();
        return info.getAvailableSpace() >= fileSize;
    }
}
