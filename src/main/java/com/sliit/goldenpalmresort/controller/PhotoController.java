package com.sliit.goldenpalmresort.controller;

import com.sliit.goldenpalmresort.dto.PhotoResponse;
import com.sliit.goldenpalmresort.model.Photo;
import com.sliit.goldenpalmresort.service.PhotoService;
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
    public ResponseEntity<PhotoResponse> uploadRoomPhoto(
            @PathVariable Long roomId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("uploadedBy") String uploadedBy) {
        try {
            PhotoResponse photo = photoService.uploadRoomPhoto(roomId, file, uploadedBy);
            return ResponseEntity.ok(photo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/event-spaces/{eventSpaceId}/upload")
    public ResponseEntity<PhotoResponse> uploadEventSpacePhoto(
            @PathVariable Long eventSpaceId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("uploadedBy") String uploadedBy) {
        try {
            PhotoResponse photo = photoService.uploadEventSpacePhoto(eventSpaceId, file, uploadedBy);
            return ResponseEntity.ok(photo);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
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
    public ResponseEntity<Resource> downloadPhoto(@PathVariable Long photoId) {
        try {
            // This would need to be implemented to get the photo from the database
            // For now, returning a placeholder response
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"photo.jpg\"")
                    .contentType(MediaType.IMAGE_JPEG)
                    .build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
} 