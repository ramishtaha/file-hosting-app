@echo off
echo 🚀 Setting up Git repository for File Hosting App
echo.

REM Initialize git if not already done
if not exist ".git" (
    echo 📝 Initializing Git repository...
    git init
    echo ✅ Git repository initialized
) else (
    echo 📁 Git repository already exists
)

REM Add all files
echo 📦 Adding files to Git...
git add .

REM Create initial commit
echo 💾 Creating initial commit...
git commit -m "Initial commit: File Hosting App" -m "" -m "🎯 Project Overview:" -m "- Cross-platform file sharing solution (iPhone ↔ Android ↔ Desktop)" -m "- Support for large files up to 20GB (perfect for videos)" -m "- Minimalist web interface with drag & drop" -m "- Built with Java 21 + Spring Boot" -m "" -m "💡 Inspiration:" -m "Created to solve a friend's problem of transferring files between iPhone and Android devices." -m "" -m "✨ Features:" -m "- File upload/download with progress tracking" -m "- Zip functionality for bulk downloads" -m "- Real-time storage monitoring" -m "- Responsive design for all devices" -m "- Self-hosted solution for privacy" -m "" -m "🛠️ Tech Stack:" -m "- Java 21, Spring Boot 3.2, Thymeleaf" -m "- H2 Database, Bootstrap 5" -m "- Maven, Docker-ready"

echo ✅ Initial commit created

echo.
echo 🌟 Next steps to push to GitHub:
echo 1. Create a new repository on GitHub
echo 2. Run these commands:
echo    git remote add origin https://github.com/YOUR_USERNAME/file-hosting-app.git
echo    git branch -M main
echo    git push -u origin main
echo.
echo 📱 Perfect for: iPhone ↔ Android file transfers, large video sharing, home networks
pause
