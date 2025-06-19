#!/bin/bash

# Quick Server Status Check for File Hosting App
# Optimized for 8GB RAM, 2 Core CPU, 160GB NVMe Server

echo "📊 File Hosting Server Status - $(date)"
echo "================================================"

# System Resources
echo "🖥️  SYSTEM RESOURCES:"
echo "   RAM Usage: $(free -h | awk '/^Mem:/ {print $3 "/" $2 " (" int($3/$2 * 100) "%)"}')"
echo "   CPU Load:  $(uptime | awk -F'load average:' '{print $2}' | xargs)"
echo "   Disk:      $(df -h /opt/filehosting 2>/dev/null | awk 'NR==2 {print $3 "/" $2 " (" $5 ")"}')"

# Service Status
echo ""
echo "🔧 SERVICE STATUS:"
if systemctl is-active --quiet filehosting; then
    echo "   ✅ File Hosting App: RUNNING"
    PID=$(pgrep -f file-hosting-app)
    if [ ! -z "$PID" ]; then
        MEM=$(ps -p $PID -o rss= | awk '{print int($1/1024) "MB"}')
        CPU=$(ps -p $PID -o %cpu= | awk '{print $1 "%"}')
        echo "   📈 Process Stats: PID=$PID, Memory=$MEM, CPU=$CPU"
    fi
else
    echo "   ❌ File Hosting App: STOPPED"
fi

# Storage Stats
echo ""
echo "📁 STORAGE STATS:"
if [ -d "/opt/filehosting/uploads" ]; then
    UPLOAD_SIZE=$(du -sh /opt/filehosting/uploads 2>/dev/null | cut -f1)
    FILE_COUNT=$(find /opt/filehosting/uploads -type f 2>/dev/null | wc -l)
    echo "   📦 Uploads: $UPLOAD_SIZE ($FILE_COUNT files)"
else
    echo "   📦 Uploads: Directory not found"
fi

# Network Status
echo ""
echo "🌐 NETWORK STATUS:"
if netstat -tuln 2>/dev/null | grep -q ":8080"; then
    CONNECTIONS=$(netstat -an 2>/dev/null | grep ":8080" | grep ESTABLISHED | wc -l)
    echo "   ✅ Port 8080: LISTENING ($CONNECTIONS active connections)"
else
    echo "   ❌ Port 8080: NOT LISTENING"
fi

# Recent Activity
echo ""
echo "📜 RECENT ACTIVITY:"
if [ -f "/var/log/filehosting/application.log" ]; then
    echo "   Last 3 log entries:"
    tail -n 3 /var/log/filehosting/application.log | sed 's/^/   /'
else
    echo "   No application logs found"
fi

# Health Check
echo ""
echo "🏥 HEALTH CHECK:"
if curl -s -o /dev/null -w "%{http_code}" http://localhost:8080 | grep -q "200"; then
    echo "   ✅ HTTP Response: OK (200)"
else
    echo "   ❌ HTTP Response: FAILED"
fi

# Quick Recommendations
echo ""
echo "💡 QUICK STATUS:"
MEMORY_PERCENT=$(free | awk '/^Mem:/ {print int($3/$2 * 100)}')
DISK_PERCENT=$(df /opt/filehosting 2>/dev/null | awk 'NR==2 {print int($3/$2 * 100)}')

if [ $MEMORY_PERCENT -gt 85 ]; then
    echo "   ⚠️  High memory usage ($MEMORY_PERCENT%)"
fi

if [ $DISK_PERCENT -gt 85 ]; then
    echo "   ⚠️  High disk usage ($DISK_PERCENT%)"
fi

if [ $MEMORY_PERCENT -le 85 ] && [ $DISK_PERCENT -le 85 ]; then
    echo "   ✅ All systems normal"
fi

echo ""
echo "🛠️  Management Commands:"
echo "   systemctl status filehosting    # Detailed service status"
echo "   journalctl -u filehosting -f    # Live logs"
echo "   /opt/filehosting/monitor.sh     # Detailed monitoring"
echo "   /opt/filehosting/backup.sh      # Create backup"
echo ""
