package com.sliit.goldenpalmresort.service;

import com.sliit.goldenpalmresort.dto.PhotoResponse;
import com.sliit.goldenpalmresort.model.Photo;
import com.sliit.goldenpalmresort.model.Room;
import com.sliit.goldenpalmresort.model.EventSpace;
import com.sliit.goldenpalmresort.repository.PhotoRepository;
import com.sliit.goldenpalmresort.repository.RoomRepository;
import com.sliit.goldenpalmresort.repository.EventSpaceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PhotoService {
    
    @Autowired
    private PhotoRepository photoRepository;
    
    @Autowired
    private RoomRepository roomRepository;
    
    @Autowired
    private EventSpaceRepository eventSpaceRepository;
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    @Value("${app.max.photos.per.item:5}")
    private int maxPhotosPerItem;
    
    public List<PhotoResponse> getRoomPhotos(Long roomId) {
        List<Photo> photos = photoRepository.findByRoomIdOrderByDisplayOrder(roomId);
        return photos.stream()
                .map(this::convertToPhotoResponse)
                .collect(Collectors.toList());
    }
    
    public List<PhotoResponse> getEventSpacePhotos(Long eventSpaceId) {
        List<Photo> photos = photoRepository.findByEventSpaceIdOrderByDisplayOrder(eventSpaceId);
        return photos.stream()
                .map(this::convertToPhotoResponse)
                .collect(Collectors.toList());
    }
    
    public PhotoResponse uploadRoomPhoto(Long roomId, MultipartFile file, String uploadedBy) throws IOException {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        
        // Check photo limit
        Long photoCount = photoRepository.countByRoomId(roomId);
        if (photoCount >= maxPhotosPerItem) {
            throw new RuntimeException("Maximum " + maxPhotosPerItem + " photos allowed per room");
        }
        
        return uploadPhoto(file, uploadedBy, room, null);
    }
    
    public PhotoResponse uploadEventSpacePhoto(Long eventSpaceId, MultipartFile file, String uploadedBy) throws IOException {
        EventSpace eventSpace = eventSpaceRepository.findById(eventSpaceId)
                .orElseThrow(() -> new RuntimeException("Event space not found"));
        
        // Check photo limit
        Long photoCount = photoRepository.countByEventSpaceId(eventSpaceId);
        if (photoCount >= maxPhotosPerItem) {
            throw new RuntimeException("Maximum " + maxPhotosPerItem + " photos allowed per event space");
        }
        
        return uploadPhoto(file, uploadedBy, null, eventSpace);
    }
    
    private PhotoResponse uploadPhoto(MultipartFile file, String uploadedBy, Room room, EventSpace eventSpace) throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        
        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Only image files are allowed");
        }
        
        // Generate unique filename
        String originalFileName = file.getOriginalFilename();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + fileExtension;
        
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // Save file
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Get next display order
        Integer nextDisplayOrder = 1;
        if (room != null) {
            Integer maxOrder = photoRepository.findMaxDisplayOrderByRoomId(room.getId());
            if (maxOrder != null) {
                nextDisplayOrder = maxOrder + 1;
            }
        } else if (eventSpace != null) {
            Integer maxOrder = photoRepository.findMaxDisplayOrderByEventSpaceId(eventSpace.getId());
            if (maxOrder != null) {
                nextDisplayOrder = maxOrder + 1;
            }
        }
        
        // Save photo record
        Photo photo = new Photo();
        photo.setFileName(fileName);
        photo.setOriginalFileName(originalFileName);
        photo.setContentType(contentType);
        photo.setFileSize(file.getSize());
        photo.setFilePath(filePath.toString());
        photo.setDisplayOrder(nextDisplayOrder);
        photo.setRoom(room);
        photo.setEventSpace(eventSpace);
        photo.setUploadedBy(uploadedBy);
        photo.setUploadedAt(LocalDateTime.now());
        photo.setIsActive(true);
        
        Photo savedPhoto = photoRepository.save(photo);
        return convertToPhotoResponse(savedPhoto);
    }
    
    public void deletePhoto(Long photoId) {
        Photo photo = photoRepository.findById(photoId)
                .orElseThrow(() -> new RuntimeException("Photo not found"));
        
        // Delete file from filesystem
        try {
            Path filePath = Paths.get(photo.getFilePath());
            Files.deleteIfExists(filePath);
        } catch (IOException e) {
            // Log error but don't throw exception
            System.err.println("Error deleting file: " + e.getMessage());
        }
        
        // Delete from database
        photoRepository.delete(photo);
    }
    
    public void reorderPhotos(List<Long> photoIds) {
        for (int i = 0; i < photoIds.size(); i++) {
            Photo photo = photoRepository.findById(photoIds.get(i))
                    .orElseThrow(() -> new RuntimeException("Photo not found"));
            photo.setDisplayOrder(i + 1);
            photoRepository.save(photo);
        }
    }
    
    private PhotoResponse convertToPhotoResponse(Photo photo) {
        PhotoResponse response = new PhotoResponse();
        response.setId(photo.getId());
        response.setFileName(photo.getFileName());
        response.setOriginalFileName(photo.getOriginalFileName());
        response.setFileSize(photo.getFileSize());
        response.setFilePath(photo.getFilePath());
        response.setDisplayOrder(photo.getDisplayOrder());
        response.setUploadedBy(photo.getUploadedBy());
        response.setUploadedAt(photo.getUploadedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        response.setDownloadUrl("/api/photos/" + photo.getId() + "/download");
        return response;
    }
    
    // New methods for database storage
    public Photo uploadRoomPhotoToDatabase(Long roomId, MultipartFile file, String uploadedBy) throws IOException {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        
        // Validate file
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        
        // Check max photos limit
        long photoCount = photoRepository.findByRoomIdOrderByDisplayOrder(roomId).size();
        if (photoCount >= maxPhotosPerItem) {
            throw new RuntimeException("Maximum " + maxPhotosPerItem + " photos allowed per room");
        }
        
        // Create photo entity
        Photo photo = new Photo();
        photo.setFileName(UUID.randomUUID().toString() + "_" + file.getOriginalFilename());
        photo.setOriginalFileName(file.getOriginalFilename());
        photo.setContentType(file.getContentType());
        photo.setFileSize(file.getSize());
        photo.setFilePath("temp"); // Will be updated after save with actual ID
        photo.setPhotoData(file.getBytes()); // Store in database
        photo.setDisplayOrder((int) photoCount + 1);
        photo.setRoom(room);
        photo.setIsActive(true);
        photo.setUploadedBy(uploadedBy);
        photo.setUploadedAt(LocalDateTime.now());
        
        photo = photoRepository.save(photo);
        
        // Update file path with actual ID
        photo.setFilePath("/api/photos/" + photo.getId() + "/download");
        return photoRepository.save(photo);
    }
    
    public Photo uploadEventSpacePhotoToDatabase(Long eventSpaceId, MultipartFile file, String uploadedBy) throws IOException {
        EventSpace eventSpace = eventSpaceRepository.findById(eventSpaceId)
                .orElseThrow(() -> new RuntimeException("Event space not found"));
        
        // Validate file
        if (file.isEmpty()) {
            throw new RuntimeException("File is empty");
        }
        
        // Check max photos limit
        long photoCount = photoRepository.findByEventSpaceIdOrderByDisplayOrder(eventSpaceId).size();
        if (photoCount >= maxPhotosPerItem) {
            throw new RuntimeException("Maximum " + maxPhotosPerItem + " photos allowed per event space");
        }
        
        // Create photo entity
        Photo photo = new Photo();
        photo.setFileName(UUID.randomUUID().toString() + "_" + file.getOriginalFilename());
        photo.setOriginalFileName(file.getOriginalFilename());
        photo.setContentType(file.getContentType());
        photo.setFileSize(file.getSize());
        photo.setFilePath("temp"); // Will be updated after save with actual ID
        photo.setPhotoData(file.getBytes()); // Store in database
        photo.setDisplayOrder((int) photoCount + 1);
        photo.setEventSpace(eventSpace);
        photo.setIsActive(true);
        photo.setUploadedBy(uploadedBy);
        photo.setUploadedAt(LocalDateTime.now());
        
        photo = photoRepository.save(photo);
        
        // Update file path with actual ID
        photo.setFilePath("/api/photos/" + photo.getId() + "/download");
        return photoRepository.save(photo);
    }
    
    public Photo getPhotoById(Long photoId) {
        return photoRepository.findById(photoId).orElse(null);
    }
}