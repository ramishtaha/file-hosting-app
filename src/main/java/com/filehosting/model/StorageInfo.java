package com.filehosting.model;

public class StorageInfo {
    private long totalSpace;
    private long usedSpace;
    private long availableSpace;
    private double usagePercentage;
    private long fileCount;
    
    public StorageInfo() {
    }
    
    public StorageInfo(long totalSpace, long usedSpace, long fileCount) {
        this.totalSpace = totalSpace;
        this.usedSpace = usedSpace;
        this.availableSpace = totalSpace - usedSpace;
        this.usagePercentage = totalSpace > 0 ? (double) usedSpace / totalSpace * 100 : 0;
        this.fileCount = fileCount;
    }
    
    // Getters and Setters
    public long getTotalSpace() {
        return totalSpace;
    }
    
    public void setTotalSpace(long totalSpace) {
        this.totalSpace = totalSpace;
    }
    
    public long getUsedSpace() {
        return usedSpace;
    }
    
    public void setUsedSpace(long usedSpace) {
        this.usedSpace = usedSpace;
    }
    
    public long getAvailableSpace() {
        return availableSpace;
    }
    
    public void setAvailableSpace(long availableSpace) {
        this.availableSpace = availableSpace;
    }
    
    public double getUsagePercentage() {
        return usagePercentage;
    }
    
    public void setUsagePercentage(double usagePercentage) {
        this.usagePercentage = usagePercentage;
    }
    
    public long getFileCount() {
        return fileCount;
    }
    
    public void setFileCount(long fileCount) {
        this.fileCount = fileCount;
    }
    
    // Utility methods
    public String getFormattedTotalSpace() {
        return formatBytes(totalSpace);
    }
    
    public String getFormattedUsedSpace() {
        return formatBytes(usedSpace);
    }
    
    public String getFormattedAvailableSpace() {
        return formatBytes(availableSpace);
    }
    
    public String getFormattedUsagePercentage() {
        return String.format("%.1f%%", usagePercentage);
    }
    
    private String formatBytes(long bytes) {
        String[] units = {"B", "KB", "MB", "GB", "TB"};
        int unitIndex = 0;
        double size = bytes;
        
        while (size >= 1024 && unitIndex < units.length - 1) {
            size /= 1024;
            unitIndex++;
        }
        
        return String.format("%.1f %s", size, units[unitIndex]);
    }
}
