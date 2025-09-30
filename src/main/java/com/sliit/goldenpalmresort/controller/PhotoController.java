package com.sliit.goldenpalmresort.controller;

import com.sliit.goldenpalmresort.dto.PhotoResponse;
import com.sliit.goldenpalmresort.model.Photo;
import com.sliit.goldenpalmresort.service.PhotoService;
import java.util.Map;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/photos")
@CrossOrigin(origins = "*")
public class PhotoController {
    
    @Autowired
    private PhotoService photoService;
    
    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<List<PhotoResponse>> getRoomPhotos(@PathVariable Long roomId) {
        try {
            List<PhotoResponse> photos = photoService.getRoomPhotos(roomId);
            return ResponseEntity.ok(photos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/event-spaces/{eventSpaceId}")
    public ResponseEntity<List<PhotoResponse>> getEventSpacePhotos(@PathVariable Long eventSpaceId) {
        try {
            List<PhotoResponse> photos = photoService.getEventSpacePhotos(eventSpaceId);
            return ResponseEntity.ok(photos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/rooms/{roomId}/upload")
    public ResponseEntity<Map<String, Object>> uploadRoomPhoto(
            @PathVariable Long roomId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "uploadedBy", defaultValue = "admin") String uploadedBy) {
        try {
            Photo photo = photoService.uploadRoomPhotoToDatabase(roomId, file, uploadedBy);
            Map<String, Object> response = new HashMap<>();
            response.put("id", photo.getId());
            response.put("fileName", photo.getFileName());
            response.put("message", "Photo uploaded successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/event-spaces/{eventSpaceId}/upload")
    public ResponseEntity<Map<String, Object>> uploadEventSpacePhoto(
            @PathVariable Long eventSpaceId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "uploadedBy", defaultValue = "admin") String uploadedBy) {
        try {
            Photo photo = photoService.uploadEventSpacePhotoToDatabase(eventSpaceId, file, uploadedBy);
            Map<String, Object> response = new HashMap<>();
            response.put("id", photo.getId());
            response.put("fileName", photo.getFileName());
            response.put("message", "Photo uploaded successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
    
    @DeleteMapping("/{photoId}")
    public ResponseEntity<Void> deletePhoto(@PathVariable Long photoId) {
        try {
            photoService.deletePhoto(photoId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/reorder")
    public ResponseEntity<Void> reorderPhotos(@RequestBody List<Long> photoIds) {
        try {
            photoService.reorderPhotos(photoIds);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{photoId}/download")
    public ResponseEntity<byte[]> downloadPhoto(@PathVariable Long photoId) {
        try {
            Photo photo = photoService.getPhotoById(photoId);
            if (photo == null || photo.getPhotoData() == null) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + photo.getFileName() + "\"")
                    .contentType(MediaType.parseMediaType(photo.getContentType()))
                    .body(photo.getPhotoData());
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
} 