package com.sliit.goldenpalmresort.repository;

import com.sliit.goldenpalmresort.model.Photo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhotoRepository extends JpaRepository<Photo, Long> {
    
    @Query("SELECT p FROM Photo p WHERE p.room.id = :roomId AND p.isActive = true ORDER BY p.displayOrder")
    List<Photo> findByRoomIdOrderByDisplayOrder(@Param("roomId") Long roomId);
    
    @Query("SELECT p FROM Photo p WHERE p.eventSpace.id = :eventSpaceId AND p.isActive = true ORDER BY p.displayOrder")
    List<Photo> findByEventSpaceIdOrderByDisplayOrder(@Param("eventSpaceId") Long eventSpaceId);
    
    @Query("SELECT COUNT(p) FROM Photo p WHERE p.room.id = :roomId AND p.isActive = true")
    Long countByRoomId(@Param("roomId") Long roomId);
    
    @Query("SELECT COUNT(p) FROM Photo p WHERE p.eventSpace.id = :eventSpaceId AND p.isActive = true")
    Long countByEventSpaceId(@Param("eventSpaceId") Long eventSpaceId);
    
    @Query("SELECT MAX(p.displayOrder) FROM Photo p WHERE p.room.id = :roomId AND p.isActive = true")
    Integer findMaxDisplayOrderByRoomId(@Param("roomId") Long roomId);
    
    @Query("SELECT MAX(p.displayOrder) FROM Photo p WHERE p.eventSpace.id = :eventSpaceId AND p.isActive = true")
    Integer findMaxDisplayOrderByEventSpaceId(@Param("eventSpaceId") Long eventSpaceId);
} 