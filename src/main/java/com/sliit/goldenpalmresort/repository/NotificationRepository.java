package com.sliit.goldenpalmresort.repository;

import com.sliit.goldenpalmresort.model.Notification;
import com.sliit.goldenpalmresort.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // Find all notifications for a user
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    
    // Find unread notifications for a user
    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);
    
    // Find notifications by type
    List<Notification> findByType(Notification.NotificationType type);
    
    // Find notifications by user and type
    List<Notification> findByUserAndTypeOrderByCreatedAtDesc(User user, Notification.NotificationType type);
    
    // Count unread notifications for a user
    long countByUserAndIsReadFalse(User user);
    
    // Find notifications by reference
    @Query("SELECT n FROM Notification n WHERE n.referenceId = :referenceId AND n.referenceType = :referenceType")
    List<Notification> findByReference(@Param("referenceId") Long referenceId, @Param("referenceType") String referenceType);
    
    // Find notifications sent by a specific staff member
    List<Notification> findBySentByOrderByCreatedAtDesc(String sentBy);
}
