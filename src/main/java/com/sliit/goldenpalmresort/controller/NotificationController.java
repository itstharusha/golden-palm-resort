package com.sliit.goldenpalmresort.controller;

import com.sliit.goldenpalmresort.model.Notification;
import com.sliit.goldenpalmresort.model.User;
import com.sliit.goldenpalmresort.repository.UserRepository;
import com.sliit.goldenpalmresort.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = "*")
public class NotificationController {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserRepository userRepository;
    
    /**
     * Get all notifications for the current user
     */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getUserNotifications(Authentication authentication) {
        try {
            String username = authentication.getName();
            Optional<User> userOpt = userRepository.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            User user = userOpt.get();
            List<Notification> notifications = notificationService.getUserNotifications(user);
            
            List<Map<String, Object>> notificationData = notifications.stream().map(notif -> {
                Map<String, Object> data = new HashMap<>();
                data.put("id", notif.getId());
                data.put("type", notif.getType());
                data.put("title", notif.getTitle());
                data.put("message", notif.getMessage());
                data.put("referenceId", notif.getReferenceId());
                data.put("referenceType", notif.getReferenceType());
                data.put("isRead", notif.isRead());
                data.put("createdAt", notif.getCreatedAt());
                data.put("readAt", notif.getReadAt());
                data.put("sentBy", notif.getSentBy());
                
                return data;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(notificationData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get unread notifications for the current user
     */
    @GetMapping("/unread")
    public ResponseEntity<List<Map<String, Object>>> getUnreadNotifications(Authentication authentication) {
        try {
            String username = authentication.getName();
            Optional<User> userOpt = userRepository.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            User user = userOpt.get();
            List<Notification> notifications = notificationService.getUnreadNotifications(user);
            
            List<Map<String, Object>> notificationData = notifications.stream().map(notif -> {
                Map<String, Object> data = new HashMap<>();
                data.put("id", notif.getId());
                data.put("type", notif.getType());
                data.put("title", notif.getTitle());
                data.put("message", notif.getMessage());
                data.put("referenceId", notif.getReferenceId());
                data.put("referenceType", notif.getReferenceType());
                data.put("isRead", notif.isRead());
                data.put("createdAt", notif.getCreatedAt());
                data.put("sentBy", notif.getSentBy());
                
                return data;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(notificationData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get unread notification count
     */
    @GetMapping("/unread/count")
    public ResponseEntity<Map<String, Object>> getUnreadCount(Authentication authentication) {
        try {
            String username = authentication.getName();
            Optional<User> userOpt = userRepository.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            User user = userOpt.get();
            long count = notificationService.getUnreadCount(user);
            
            return ResponseEntity.ok(Map.of("count", count));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Mark notification as read
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markAsRead(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication.getName();
            Optional<User> userOpt = userRepository.findByUsername(username);
            Optional<Notification> notifOpt = notificationService.getNotificationById(id);
            
            if (userOpt.isEmpty() || notifOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Notification not found"));
            }
            
            Notification notification = notifOpt.get();
            User user = userOpt.get();
            
            // Verify the notification belongs to the user
            if (!notification.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
            }
            
            notificationService.markAsRead(id);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Notification marked as read"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to mark notification as read"));
        }
    }
    
    /**
     * Mark all notifications as read
     */
    @PutMapping("/read-all")
    public ResponseEntity<Map<String, Object>> markAllAsRead(Authentication authentication) {
        try {
            String username = authentication.getName();
            Optional<User> userOpt = userRepository.findByUsername(username);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            
            User user = userOpt.get();
            notificationService.markAllAsRead(user);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "All notifications marked as read"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to mark all notifications as read"));
        }
    }
    
    /**
     * Delete a notification
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteNotification(@PathVariable Long id, Authentication authentication) {
        try {
            String username = authentication.getName();
            Optional<User> userOpt = userRepository.findByUsername(username);
            Optional<Notification> notifOpt = notificationService.getNotificationById(id);
            
            if (userOpt.isEmpty() || notifOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Notification not found"));
            }
            
            Notification notification = notifOpt.get();
            User user = userOpt.get();
            
            // Verify the notification belongs to the user
            if (!notification.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body(Map.of("error", "Unauthorized"));
            }
            
            notificationService.deleteNotification(id);
            
            return ResponseEntity.ok(Map.of("success", true, "message", "Notification deleted"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to delete notification"));
        }
    }
}
