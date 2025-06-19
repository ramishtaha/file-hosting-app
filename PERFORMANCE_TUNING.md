# Performance Tuning Guide for 8GB RAM, 2 Core CPU, 160GB NVMe Server

## 🚀 Optimized JVM Settings

### Production JVM Arguments
```bash
java -Xms1g \
     -Xmx6g \
     -XX:+UseG1GC \
     -XX:MaxGCPauseMillis=200 \
     -XX:+UseStringDeduplication \
     -XX:+OptimizeStringConcat \
     -XX:+UseCompressedOops \
     -XX:+UseCompressedClassPointers \
     -jar file-hosting-app.jar
```

### Memory Allocation Strategy
- **JVM Heap**: 6GB (75% of 8GB RAM)
- **System**: 2GB (OS + other processes)
- **Reasoning**: Leaves enough memory for OS and file system cache

## 🔧 Application Configuration

### Thread Pool Optimization
```yaml
server:
  tomcat:
    threads:
      max: 50          # 25 threads per CPU core
      min-spare: 10    # Always keep 10 threads ready
    max-connections: 200  # Conservative for memory usage
    accept-count: 100     # Queue size for pending connections
```

### Database Connection Pool
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size: 8    # 4 connections per CPU core
      minimum-idle: 2         # Keep 2 idle connections
      connection-timeout: 20000
      idle-timeout: 300000    # 5 minutes
      max-lifetime: 1200000   # 20 minutes
```

### File Handling Optimization
```yaml
spring:
  http:
    multipart:
      file-size-threshold: 50MB  # Larger threshold for NVMe SSD
      location: /tmp             # Use fast SSD for temp files
```

## 💾 Storage Configuration

### Directory Structure
```
/opt/filehosting/
├── uploads/          # 120GB allocated (75% of 160GB)
├── data/             # Database files
├── logs/             # Application logs
├── backups/          # Automated backups
└── temp/             # Temporary files
```

### NVMe SSD Optimization
- **Upload Limit**: 120GB (leaves 40GB for OS, logs, temp files)
- **Temp File Threshold**: 50MB (takes advantage of fast SSD writes)
- **File System**: Use ext4 or xfs for optimal performance

## ⚡ Performance Monitoring

### Key Metrics to Watch
1. **Memory Usage**: Should stay under 7GB total
2. **CPU Usage**: Should average < 80% under load
3. **Disk Usage**: Monitor upload directory size
4. **Thread Count**: Watch for thread pool exhaustion

### Monitoring Commands
```bash
# Overall system status
/opt/filehosting/monitor.sh

# Memory usage
free -h

# Disk usage
df -h /opt/filehosting

# Active connections
ss -tuln | grep :8080

# JVM metrics
jstat -gc $(pgrep java) 5s
```

## 🔍 Troubleshooting

### Memory Issues
- **Symptom**: OutOfMemoryError
- **Solution**: Reduce max-connections or increase file-size-threshold

### CPU Bottlenecks
- **Symptom**: High CPU usage, slow response
- **Solution**: Reduce thread pool size, enable response compression

### Disk Space Issues
- **Symptom**: Upload failures
- **Solution**: Automated cleanup, monitoring scripts

### Large File Upload Issues
- **Symptom**: Timeouts, connection drops
- **Solution**: Increase connection-timeout, check network stability

## 🛠️ Production Optimizations

### Nginx Reverse Proxy (Recommended)
```nginx
server {
    listen 80;
    client_max_body_size 20G;
    client_timeout 600s;
    
    location / {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_request_buffering off;  # For large file uploads
    }
}
```

### Firewall Configuration
```bash
# Allow HTTP and SSH only
ufw allow 22/tcp
ufw allow 80/tcp
ufw allow 8080/tcp
ufw enable
```

### Automated Backups
```bash
# Add to crontab
0 2 * * * /opt/filehosting/backup.sh > /var/log/backup.log 2>&1
```

## 📊 Expected Performance

### Concurrent Users
- **Light Usage**: 20-30 concurrent users
- **Heavy Usage**: 10-15 concurrent large file uploads
- **Peak Capacity**: 50 simultaneous connections

### File Transfer Speeds
- **Upload**: Limited by client connection speed
- **Download**: Up to 100MB/s (NVMe SSD limited)
- **Multiple Transfers**: Bandwidth shared among active transfers

### Storage Capacity
- **Total**: 120GB for user files
- **Recommended Usage**: Keep under 100GB for optimal performance
- **File Count**: Estimated 10,000-50,000 files depending on size

## 🚨 Alerts & Monitoring

### Set up alerts for:
- Disk usage > 90%
- Memory usage > 7GB
- CPU usage > 90% for 5+ minutes
- Application downtime
- Failed upload attempts

This configuration provides optimal performance for your server specs while maintaining stability and allowing for growth.
