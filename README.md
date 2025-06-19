# File Hosting Web Application

A minimalist file hosting web application built with Java 21 and Spring Boot. Features include file upload/download, zip functionality, storage monitoring, and a clean responsive interface.

## 💡 Inspiration

This project was born from a practical problem. A friend asked me: **"How can I transfer files between my iPhone and Android device?"** 

While there are many existing solutions, I thought - why not create a simple, self-hosted web application that works across all devices? This led to the development of this minimalist file hosting service that can be easily deployed anywhere and accessed from any device with a web browser.

**Perfect for:**
- 📱 **Cross-platform file sharing** (iPhone ↔ Android ↔ Desktop)
- 🏠 **Home networks** where you want to share files between devices
- 🔒 **Privacy-conscious users** who prefer self-hosted solutions
- 🚀 **Quick deployments** when you need a temporary file sharing solution

## Features

- 📁 **File Upload & Download**: Support for images, videos, documents, and archives (up to 20GB per file)
- 🗜️ **Zip Functionality**: Download individual files or create zip archives of selected files
- 📊 **Storage Monitoring**: Real-time storage usage visualization with configurable limits
- 🔍 **File Management**: Search, filter, and organize uploaded files
- 📱 **Responsive Design**: Works perfectly on iPhone, Android, and desktop devices
- 🚀 **Easy Deployment**: Single JAR file deployment suitable for cloud platforms
- 🌐 **Cross-Platform**: Access from any device with a web browser

## Technology Stack

- **Java 21** (LTS)
- **Spring Boot 3.2.0**
- **Spring Data JPA** with H2 database
- **Thymeleaf** for templating
- **Bootstrap 5** for UI
- **Maven** for build management

## Quick Start

### Prerequisites

- Java 21 or higher
- Maven 3.6+ (optional - can use included wrapper)

### Local Development

1. **Clone and navigate to the project:**
   ```bash
   cd file_hosting
   ```

2. **Run the application:**
   ```bash
   # Using Maven wrapper (recommended)
   ./mvnw spring-boot:run
   
   # Or using Maven directly
   mvn spring-boot:run
   ```

3. **Access the application:**
   - Open your browser and go to `http://localhost:8080`
   - The application will create an `uploads` folder automatically
   - Database files will be created in the `data` folder

### Building for Production

```bash
# Build the JAR file
./mvnw clean package

# The executable JAR will be in target/
java -jar target/file-hosting-app-1.0.0.jar
```

## Large File Support

This application supports files up to **20GB** in size, making it perfect for sharing large video files between devices:

### 📹 **Optimized for Video Sharing**
- Support for large video files (4K recordings, long videos, etc.)
- Efficient streaming for file downloads
- Progress indicators for large uploads
- Automatic cleanup of temporary files

### ⚡ **Performance Considerations**
- Files larger than 10MB are written to disk during upload (not kept in memory)
- Configurable connection timeouts for large file transfers
- Efficient file streaming for downloads
- Background processing for zip creation

### 🔧 **System Requirements for Large Files**
- **RAM**: Minimum 2GB recommended, **8GB optimal for 20GB file support**
- **Disk Space**: Ensure sufficient space for uploads + temporary files
- **Network**: Stable connection for large file transfers
- **Java Heap**: **6GB recommended for 8GB RAM servers** (`-Xmx6g`)

## 🖥️ **Production Server Optimization (8GB RAM, 2 Core, 160GB NVMe)**

For your specific server configuration, the app is optimized with:
- **Memory Allocation**: 6GB JVM heap (75% of 8GB RAM)
- **Thread Pool**: 50 max threads (25 per CPU core)
- **Storage**: 120GB for uploads (75% of 160GB NVMe)
- **Temp Files**: 50MB threshold for NVMe SSD optimization

### Quick Production Deployment
```bash
chmod +x deploy-production.sh
./deploy-production.sh
```
See `PERFORMANCE_TUNING.md` for detailed optimization guide.

## Configuration

### Application Properties

The application can be configured through environment variables or `application.yml`:

```yaml
app:
  upload:
    path: ${UPLOAD_PATH:./uploads}        # Upload directory path
    max-size-gb: ${MAX_STORAGE_GB:50}     # Maximum storage in GB

server:
  port: ${PORT:8080}                      # Server port

spring:
  servlet:
    multipart:
      max-file-size: ${MAX_FILE_SIZE:20GB}      # Max individual file size (supports large videos)
      max-request-size: ${MAX_REQUEST_SIZE:20GB} # Max total request size
```

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `UPLOAD_PATH` | `./uploads` | Directory where uploaded files are stored |
| `MAX_STORAGE_GB` | `120` | Maximum storage capacity in GB (optimized for 160GB server) |
| `PORT` | `8080` | Server port |
| `MAX_FILE_SIZE` | `20GB` | Maximum individual file size (perfect for large videos) |
| `MAX_REQUEST_SIZE` | `20GB` | Maximum total request size |

## Deployment

### Local Testing with WSL

1. **Install Java 21 in WSL:**
   ```bash
   # Ubuntu/Debian
   sudo apt update
   sudo apt install openjdk-21-jdk
   
   # Verify installation
   java -version
   ```

2. **Build and run:**
   ```bash
   ./mvnw clean package
   java -jar target/file-hosting-app-1.0.0.jar
   ```

3. **Set custom configuration:**
   ```bash
   # Example with custom settings
   UPLOAD_PATH=/var/uploads MAX_STORAGE_GB=50 java -jar target/file-hosting-app-1.0.0.jar
   ```

### DigitalOcean Deployment

#### Option 1: Droplet Deployment

1. **Create a DigitalOcean Droplet:**
   - Choose Ubuntu 22.04 LTS
   - Minimum 1GB RAM (2GB recommended)
   - Add your SSH key

2. **Connect and setup:**
   ```bash
   # Connect to your droplet
   ssh root@your-droplet-ip
   
   # Update system
   apt update && apt upgrade -y
   
   # Install Java 21
   apt install openjdk-21-jdk -y
   
   # Create application user
   useradd -m -s /bin/bash filehosting
   su - filehosting
   ```

3. **Deploy application:**
   ```bash
   # Upload your JAR file (from local machine)
   scp target/file-hosting-app-1.0.0.jar root@your-droplet-ip:/home/filehosting/
   
   # On the droplet
   chown filehosting:filehosting /home/filehosting/file-hosting-app-1.0.0.jar
   ```

4. **Create systemd service:**
   ```bash
   # Create service file
   sudo nano /etc/systemd/system/filehosting.service
   ```

   Add this content:
   ```ini
   [Unit]
   Description=File Hosting Application
   After=network.target
   
   [Service]
   Type=simple
   User=filehosting
   WorkingDirectory=/home/filehosting
   ExecStart=/usr/bin/java -jar file-hosting-app-1.0.0.jar
   Environment=UPLOAD_PATH=/home/filehosting/uploads
   Environment=MAX_STORAGE_GB=20
   Environment=PORT=8080
   Restart=always
   RestartSec=10
   
   [Install]
   WantedBy=multi-user.target
   ```

5. **Start the service:**
   ```bash
   sudo systemctl daemon-reload
   sudo systemctl enable filehosting
   sudo systemctl start filehosting
   
   # Check status
   sudo systemctl status filehosting
   ```

6. **Setup reverse proxy (optional but recommended):**
   ```bash
   # Install Nginx
   sudo apt install nginx -y
   
   # Create Nginx config
   sudo nano /etc/nginx/sites-available/filehosting
   ```

   Add this content:
   ```nginx
   server {
       listen 80;
       server_name your-domain.com;  # Replace with your domain
       
       location / {
           proxy_pass http://localhost:8080;
           proxy_set_header Host $host;
           proxy_set_header X-Real-IP $remote_addr;
           proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
           proxy_set_header X-Forwarded-Proto $scheme;
       }
       
       client_max_body_size 100M;
   }
   ```

   ```bash
   # Enable the site
   sudo ln -s /etc/nginx/sites-available/filehosting /etc/nginx/sites-enabled/
   sudo nginx -t
   sudo systemctl restart nginx
   ```

#### Option 2: App Platform Deployment

1. **Create `Dockerfile`** (optional):
   ```dockerfile
   FROM openjdk:21-jdk-slim
   
   WORKDIR /app
   COPY target/file-hosting-app-1.0.0.jar app.jar
   
   ENV UPLOAD_PATH=/app/uploads
   ENV MAX_STORAGE_GB=10
   
   EXPOSE 8080
   
   CMD ["java", "-jar", "app.jar"]
   ```

2. **Deploy to App Platform:**
   - Connect your GitHub repository
   - Set build command: `mvn clean package`
   - Set run command: `java -jar target/file-hosting-app-1.0.0.jar`
   - Configure environment variables as needed

## File Storage

- Files are stored in the configured upload directory
- File metadata is stored in an H2 database
- Original filenames are preserved in the database
- Files are stored with unique generated names to prevent conflicts

## Security Considerations

- File type validation is implemented
- File size limits are enforced
- Path traversal protection is built-in
- Consider adding authentication for production use
- Configure appropriate firewall rules for your deployment

## Development

### Project Structure

```
src/
├── main/
│   ├── java/com/filehosting/
│   │   ├── FileHostingApplication.java
│   │   ├── config/
│   │   ├── controller/
│   │   ├── model/
│   │   ├── repository/
│   │   └── service/
│   └── resources/
│       ├── static/
│       ├── templates/
│       └── application.yml
└── test/
```

### Adding Features

1. **File Previews**: Extend the UI to show image/video previews
2. **User Authentication**: Add Spring Security for user management
3. **File Sharing**: Generate shareable links with expiration
4. **Admin Panel**: Add administrative features
5. **File Versioning**: Keep multiple versions of uploaded files

### Running Tests

```bash
./mvnw test
```

## Monitoring and Maintenance

### Logs

```bash
# View application logs
sudo journalctl -u filehosting -f

# View Nginx logs (if using)
sudo tail -f /var/log/nginx/access.log
sudo tail -f /var/log/nginx/error.log
```

### Database Management

- H2 Console available at `http://localhost:8080/h2-console` (development only)
- JDBC URL: `jdbc:h2:file:./data/filehosting`
- Username: `sa`, Password: (empty)

### Backup

```bash
# Backup uploaded files
tar -czf uploads-backup-$(date +%Y%m%d).tar.gz uploads/

# Backup database
cp data/filehosting.mv.db filehosting-backup-$(date +%Y%m%d).mv.db
```

## Troubleshooting

### Common Issues

1. **Thymeleaf Template Parsing Error**
   - **Error**: `Only variable expressions returning numbers or booleans are allowed in this context`
   - **Solution**: This error occurs when using string expressions in event handlers. The fix has been implemented using data attributes instead of inline onclick handlers.

2. **Out of Memory**: Increase JVM heap size with `-Xmx512m` or higher
3. **Permission Denied**: Ensure upload directory has proper permissions
4. **File Size Limit**: Check both Spring Boot and web server limits
5. **Database Lock**: Stop application properly to avoid database locks

### Template Security

Starting with Thymeleaf 3.1, security restrictions prevent string expressions in event handlers like `onclick`. The application now uses:
- Data attributes (`data-file-id`, `data-file-name`) to pass values
- JavaScript event delegation to handle clicks securely
- This approach is more secure and follows modern web development practices

### Performance Tuning

- Adjust JVM memory settings based on usage (recommended: `-Xmx2g` for large files)
- Configure appropriate file size limits
- Consider using external storage for large deployments
- Monitor disk space regularly

### Large File Upload Issues

1. **Upload Timeout**: Increase `server.connection-timeout` in application.yml
2. **Memory Issues**: Files >10MB are automatically written to disk, not kept in memory
3. **Disk Space**: Ensure sufficient space for uploads + temporary files during processing
4. **Network Stability**: Large file uploads require stable internet connection

## 🌟 Getting Started

### Quick Start (Local)
```bash
git clone https://github.com/YOUR_USERNAME/file-hosting-app.git
cd file-hosting-app
mvn spring-boot:run
# Open http://localhost:8080
```

### 📱 Perfect for Cross-Platform File Sharing
- **iPhone to Android**: Upload photos/videos from iPhone, download on Android
- **Home Network**: Share files between all family devices  
- **Temporary Sharing**: Quick file exchange without cloud services
- **Large Video Files**: Transfer 4K videos, recordings up to 20GB

## Contributing

This project started from a simple question: *"How to transfer files between iPhone and Android?"*

Feel free to:
- 🐛 Report bugs or suggest features
- 🔧 Submit pull requests  
- 🌟 Star the repo if you find it useful
- 🔄 Fork for your own experiments

This is part of my genAI experimentation project exploring practical AI-assisted development.

## License

MIT License - feel free to use this project for personal or commercial purposes.
