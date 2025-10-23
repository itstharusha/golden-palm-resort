package com.sliit.goldenpalmresort.service;

import com.sliit.goldenpalmresort.model.Notification;
import com.sliit.goldenpalmresort.model.User;
import com.sliit.goldenpalmresort.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class NotificationService {
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    /**
     * Create and send a notification to a user
     */
    public Notification createNotification(User user, Notification.NotificationType type, 
                                          String title, String message, String sentBy) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setSentBy(sentBy);
        notification.setRead(false);
        
        return notificationRepository.save(notification);
    }
    
    /**
     * Create notification with reference (booking/event booking/refund)
     */
    public Notification createNotificationWithReference(User user, Notification.NotificationType type,
                                                       String title, String message, String sentBy,
                                                       Long referenceId, String referenceType) {
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setSentBy(sentBy);
        notification.setReferenceId(referenceId);
        notification.setReferenceType(referenceType);
        notification.setRead(false);
        
        return notificationRepository.save(notification);
    }
    
    /**
     * Get all notifications for a user
     */
    public List<Notification> getUserNotifications(User user) {
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }
    
    /**
     * Get unread notifications for a user
     */
    public List<Notification> getUnreadNotifications(User user) {
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }
    
    /**
     * Get unread notification count for a user
     */
    public long getUnreadCount(User user) {
        return notificationRepository.countByUserAndIsReadFalse(user);
    }
    
    /**
     * Mark notification as read
     */
    public void markAsRead(Long notificationId) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
            notificationRepository.save(notification);
        }
    }
    
    /**
     * Mark all notifications as read for a user
     */
    public void markAllAsRead(User user) {
        List<Notification> unreadNotifications = getUnreadNotifications(user);
        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
            notification.setReadAt(LocalDateTime.now());
        }
        notificationRepository.saveAll(unreadNotifications);
    }
    
    /**
     * Delete a notification
     */
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
    
    /**
     * Get notification by ID
     */
    public Optional<Notification> getNotificationById(Long id) {
        return notificationRepository.findById(id);
    }
    
    /**
     * Get notifications sent by a staff member
     */
    public List<Notification> getNotificationsBySender(String sentBy) {
        return notificationRepository.findBySentByOrderByCreatedAtDesc(sentBy);
    }
}
