#!/bin/bash

# Production Deployment Script for 8GB RAM, 2 Core CPU, 160GB NVMe Server
# Optimized for file hosting with large file support

echo "🚀 Deploying File Hosting App - Production Configuration"
echo "Server Specs: 8GB RAM, 2 Core CPU, 160GB NVMe"
echo ""

# Check if Java 21 is installed
if ! command -v java &> /dev/null; then
    echo "❌ Java not found. Installing OpenJDK 21..."
    sudo apt update
    sudo apt install -y openjdk-21-jdk
fi

# Verify Java version
JAVA_VERSION=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
echo "☕ Java Version: $JAVA_VERSION"

# Create application directories
echo "📁 Creating application directories..."
sudo mkdir -p /opt/filehosting
sudo mkdir -p /opt/filehosting/uploads
sudo mkdir -p /var/log/filehosting
sudo mkdir -p /opt/filehosting/backups

# Set permissions
sudo chown -R $USER:$USER /opt/filehosting
sudo chown -R $USER:$USER /var/log/filehosting

# Copy application files
echo "📦 Copying application files..."
cp target/file-hosting-app-1.0.0.jar /opt/filehosting/
cp src/main/resources/application-production.yml /opt/filehosting/

# Create systemd service file optimized for your server
echo "⚙️ Creating systemd service..."
sudo tee /etc/systemd/system/filehosting.service > /dev/null <<EOF
[Unit]
Description=File Hosting Application
After=network.target
Wants=network.target

[Service]
Type=simple
User=$USER
WorkingDirectory=/opt/filehosting
ExecStart=/usr/bin/java \\
    -Xms1g \\
    -Xmx6g \\
    -XX:+UseG1GC \\
    -XX:MaxGCPauseMillis=200 \\
    -XX:+UseStringDeduplication \\
    -XX:+OptimizeStringConcat \\
    -Dspring.profiles.active=production \\
    -Dserver.port=8080 \\
    -DUPLOAD_PATH=/opt/filehosting/uploads \\
    -DMAX_STORAGE_GB=120 \\
    -jar file-hosting-app-1.0.0.jar

# Restart policy
Restart=always
RestartSec=10

# Resource limits optimized for your server
LimitNOFILE=65536
LimitNPROC=4096

# Security settings
NoNewPrivileges=true
PrivateTmp=true

# Environment
Environment=JAVA_OPTS="-Djava.awt.headless=true"

[Install]
WantedBy=multi-user.target
EOF

# Create backup script
echo "💾 Creating backup script..."
tee /opt/filehosting/backup.sh > /dev/null <<'EOF'
#!/bin/bash
# Backup script for file hosting app

BACKUP_DIR="/opt/filehosting/backups"
DATE=$(date +%Y%m%d_%H%M%S)

echo "Creating backup: $DATE"

# Backup uploads directory
tar -czf "$BACKUP_DIR/uploads_$DATE.tar.gz" -C /opt/filehosting uploads/

# Backup database
cp /opt/filehosting/data/filehosting.mv.db "$BACKUP_DIR/database_$DATE.mv.db" 2>/dev/null || echo "No database file found"

# Cleanup old backups (keep last 7 days)
find "$BACKUP_DIR" -name "*.tar.gz" -mtime +7 -delete
find "$BACKUP_DIR" -name "*.mv.db" -mtime +7 -delete

echo "Backup completed: $DATE"
EOF

chmod +x /opt/filehosting/backup.sh

# Create monitoring script
echo "📊 Creating monitoring script..."
tee /opt/filehosting/monitor.sh > /dev/null <<'EOF'
#!/bin/bash
# Monitoring script for file hosting app

echo "=== File Hosting App Status ==="
echo "Date: $(date)"
echo ""

# Service status
echo "🔧 Service Status:"
systemctl is-active filehosting
echo ""

# Memory usage
echo "💾 Memory Usage:"
free -h
echo ""

# Disk usage
echo "💿 Disk Usage:"
df -h /opt/filehosting
echo ""

# Upload directory stats
echo "📁 Upload Directory:"
du -sh /opt/filehosting/uploads/
ls -la /opt/filehosting/uploads/ | wc -l | xargs echo "Files:"
echo ""

# Process info
echo "⚡ Process Info:"
ps aux | grep file-hosting-app | grep -v grep
echo ""

# Recent logs
echo "📜 Recent Logs (last 10 lines):"
tail -n 10 /var/log/filehosting/application.log 2>/dev/null || echo "No logs found"
EOF

chmod +x /opt/filehosting/monitor.sh

# Setup log rotation
echo "📝 Setting up log rotation..."
sudo tee /etc/logrotate.d/filehosting > /dev/null <<EOF
/var/log/filehosting/*.log {
    daily
    missingok
    rotate 7
    compress
    delaycompress
    notifempty
    copytruncate
}
EOF

# Reload systemd and enable service
echo "🔄 Enabling service..."
sudo systemctl daemon-reload
sudo systemctl enable filehosting

# Start the service
echo "▶️ Starting File Hosting App..."
sudo systemctl start filehosting

# Wait a moment and check status
sleep 5
echo ""
echo "✅ Deployment Status:"
sudo systemctl status filehosting --no-pager

echo ""
echo "🌟 Production Deployment Complete!"
echo ""
echo "📊 Server Configuration:"
echo "   • RAM: 8GB (6GB allocated to JVM)"
echo "   • CPU: 2 Cores (50 max threads)"
echo "   • Storage: 160GB NVMe (120GB for uploads)"
echo "   • File Size Limit: 20GB per file"
echo ""
echo "🛠️ Management Commands:"
echo "   • Status:  sudo systemctl status filehosting"
echo "   • Logs:    sudo journalctl -u filehosting -f"
echo "   • Restart: sudo systemctl restart filehosting"
echo "   • Monitor: /opt/filehosting/monitor.sh"
echo "   • Backup:  /opt/filehosting/backup.sh"
echo ""
echo "🌍 Access your app at: http://YOUR_SERVER_IP:8080"
echo ""
echo "💡 Pro Tips:"
echo "   • Set up nginx reverse proxy for better performance"
echo "   • Configure firewall to allow port 8080"
echo "   • Setup automated backups with cron"
echo "   • Monitor disk space regularly"
EOF
