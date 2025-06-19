// File Hosting App JavaScript

document.addEventListener('DOMContentLoaded', function() {
    initializeDropZone();
    initializeFileInput();
    initializeDeleteButtons();
});

function initializeDeleteButtons() {
    // Use event delegation for dynamically loaded content
    document.addEventListener('click', function(e) {
        if (e.target.closest('.delete-file-btn')) {
            const button = e.target.closest('.delete-file-btn');
            const fileId = button.getAttribute('data-file-id');
            const fileName = button.getAttribute('data-file-name');
            deleteFile(fileId, fileName);
        }
    });
}

function initializeDropZone() {
    const dropZone = document.getElementById('drop-zone');
    const fileInput = document.getElementById('file-input');
    
    if (!dropZone || !fileInput) return;
    
    // Click to browse
    dropZone.addEventListener('click', () => {
        fileInput.click();
    });
    
    // Drag and drop events
    dropZone.addEventListener('dragover', (e) => {
        e.preventDefault();
        dropZone.classList.add('dragover');
    });
    
    dropZone.addEventListener('dragleave', (e) => {
        e.preventDefault();
        dropZone.classList.remove('dragover');
    });
    
    dropZone.addEventListener('drop', (e) => {
        e.preventDefault();
        dropZone.classList.remove('dragover');
        
        const files = e.dataTransfer.files;
        fileInput.files = files;
        displaySelectedFiles(files);
    });
}

function initializeFileInput() {
    const fileInput = document.getElementById('file-input');
    
    if (!fileInput) return;
    
    fileInput.addEventListener('change', function() {
        displaySelectedFiles(this.files);
    });
}

function displaySelectedFiles(files) {
    const fileList = document.getElementById('file-list');
    const selectedFilesList = document.getElementById('selected-files');
    const uploadBtn = document.getElementById('upload-btn');
    
    if (!files || files.length === 0) {
        fileList.style.display = 'none';
        uploadBtn.disabled = true;
        return;
    }
    
    selectedFilesList.innerHTML = '';
    
    Array.from(files).forEach((file, index) => {
        const listItem = document.createElement('li');
        listItem.className = 'list-group-item d-flex justify-content-between align-items-center';
        
        const fileInfo = document.createElement('div');
        fileInfo.innerHTML = `
            <strong>${file.name}</strong>
            <small class="text-muted d-block">${formatFileSize(file.size)}</small>
        `;
        
        const removeBtn = document.createElement('button');
        removeBtn.className = 'btn btn-sm btn-outline-danger';
        removeBtn.innerHTML = '<i class="bi bi-x"></i>';
        removeBtn.onclick = () => removeFile(index);
        
        listItem.appendChild(fileInfo);
        listItem.appendChild(removeBtn);
        selectedFilesList.appendChild(listItem);
    });
    
    fileList.style.display = 'block';
    uploadBtn.disabled = false;
}

function removeFile(index) {
    const fileInput = document.getElementById('file-input');
    const dt = new DataTransfer();
    
    Array.from(fileInput.files).forEach((file, i) => {
        if (i !== index) {
            dt.items.add(file);
        }
    });
    
    fileInput.files = dt.files;
    displaySelectedFiles(fileInput.files);
}

function formatFileSize(bytes) {
    if (bytes === 0) return '0 B';
    
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    
    return parseFloat((bytes / Math.pow(k, i)).toFixed(1)) + ' ' + sizes[i];
}

// File selection functions
function selectAll() {
    const checkboxes = document.querySelectorAll('.file-checkbox');
    checkboxes.forEach(checkbox => {
        checkbox.checked = true;
    });
    updateSelectionButtons();
}

function clearSelection() {
    const checkboxes = document.querySelectorAll('.file-checkbox');
    checkboxes.forEach(checkbox => {
        checkbox.checked = false;
    });
    updateSelectionButtons();
}

function updateSelectionButtons() {
    const checkboxes = document.querySelectorAll('.file-checkbox');
    const checkedBoxes = document.querySelectorAll('.file-checkbox:checked');
    const downloadBtn = document.getElementById('download-selected-btn');
    
    if (downloadBtn) {
        downloadBtn.disabled = checkedBoxes.length === 0;
    }
}

function getSelectedFileIds() {
    const checkedBoxes = document.querySelectorAll('.file-checkbox:checked');
    return Array.from(checkedBoxes).map(checkbox => checkbox.value);
}

function downloadSelected() {
    const selectedIds = getSelectedFileIds();
    
    if (selectedIds.length === 0) {
        alert('Please select at least one file to download.');
        return;
    }
    
    const modal = new bootstrap.Modal(document.getElementById('downloadModal'));
    modal.show();
}

function confirmDownloadSelected() {
    const selectedIds = getSelectedFileIds();
    const zipName = document.getElementById('zip-name').value || 'selected_files';
    
    if (selectedIds.length === 0) {
        alert('Please select at least one file to download.');
        return;
    }
    
    // Create form and submit
    const form = document.createElement('form');
    form.method = 'POST';
    form.action = '/download-zip';
    
    // Add selected file IDs
    selectedIds.forEach(id => {
        const input = document.createElement('input');
        input.type = 'hidden';
        input.name = 'selectedFiles';
        input.value = id;
        form.appendChild(input);
    });
    
    // Add zip name
    const zipNameInput = document.createElement('input');
    zipNameInput.type = 'hidden';
    zipNameInput.name = 'zipName';
    zipNameInput.value = zipName;
    form.appendChild(zipNameInput);
    
    document.body.appendChild(form);
    form.submit();
    document.body.removeChild(form);
    
    // Close modal
    const modal = bootstrap.Modal.getInstance(document.getElementById('downloadModal'));
    modal.hide();
}

function deleteFile(fileId, fileName) {
    document.getElementById('delete-filename').textContent = fileName;
    document.getElementById('delete-form').action = '/delete/' + fileId;
    
    const modal = new bootstrap.Modal(document.getElementById('deleteModal'));
    modal.show();
}

// Storage monitoring
function updateStorageInfo() {
    fetch('/api/storage')
        .then(response => response.json())
        .then(data => {
            // Update storage display if needed
            console.log('Storage info updated:', data);
        })
        .catch(error => {
            console.error('Failed to update storage info:', error);
        });
}

// Auto-refresh storage info every 30 seconds
setInterval(updateStorageInfo, 30000);

// Upload progress (basic implementation)
function showUploadProgress() {
    const uploadBtn = document.getElementById('upload-btn');
    if (uploadBtn) {
        uploadBtn.innerHTML = '<span class="spinner"></span> Uploading...';
        uploadBtn.disabled = true;
    }
}

// Form submission handler
document.addEventListener('submit', function(e) {
    if (e.target.enctype === 'multipart/form-data') {
        showUploadProgress();
    }
});

// Utility functions
function copyToClipboard(text) {
    navigator.clipboard.writeText(text).then(function() {
        // Show success message
        showToast('Link copied to clipboard!', 'success');
    }).catch(function() {
        // Fallback for older browsers
        const textArea = document.createElement('textarea');
        textArea.value = text;
        document.body.appendChild(textArea);
        textArea.select();
        document.execCommand('copy');
        document.body.removeChild(textArea);
        showToast('Link copied to clipboard!', 'success');
    });
}

function showToast(message, type = 'info') {
    // Simple toast implementation
    const toast = document.createElement('div');
    toast.className = `alert alert-${type} alert-dismissible fade show position-fixed`;
    toast.style.top = '20px';
    toast.style.right = '20px';
    toast.style.zIndex = '9999';
    toast.style.minWidth = '300px';
    
    toast.innerHTML = `
        ${message}
        <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
    `;
    
    document.body.appendChild(toast);
    
    // Auto remove after 3 seconds
    setTimeout(() => {
        if (toast.parentNode) {
            toast.parentNode.removeChild(toast);
        }
    }, 3000);
}
