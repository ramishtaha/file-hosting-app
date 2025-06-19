#!/bin/bash

# GitHub Setup Script for File Hosting App
echo "🚀 Setting up Git repository for File Hosting App"
echo ""

# Initialize git if not already done
if [ ! -d ".git" ]; then
    echo "📝 Initializing Git repository..."
    git init
    echo "✅ Git repository initialized"
else
    echo "📁 Git repository already exists"
fi

# Add all files
echo "📦 Adding files to Git..."
git add .

# Create initial commit
echo "💾 Creating initial commit..."
git commit -m "Initial commit: File Hosting App

🎯 Project Overview:
- Cross-platform file sharing solution (iPhone ↔ Android ↔ Desktop)
- Support for large files up to 20GB (perfect for videos)
- Minimalist web interface with drag & drop
- Built with Java 21 + Spring Boot

💡 Inspiration:
Created to solve a friend's problem of transferring files between iPhone and Android devices.

✨ Features:
- File upload/download with progress tracking
- Zip functionality for bulk downloads
- Real-time storage monitoring
- Responsive design for all devices
- Self-hosted solution for privacy

🛠️ Tech Stack:
- Java 21, Spring Boot 3.2, Thymeleaf
- H2 Database, Bootstrap 5
- Maven, Docker-ready"

echo "✅ Initial commit created"

echo ""
echo "🌟 Next steps to push to GitHub:"
echo "1. Create a new repository on GitHub"
echo "2. Run these commands:"
echo "   git remote add origin https://github.com/YOUR_USERNAME/file-hosting-app.git"
echo "   git branch -M main"
echo "   git push -u origin main"
echo ""
echo "📱 Perfect for: iPhone ↔ Android file transfers, large video sharing, home networks"
