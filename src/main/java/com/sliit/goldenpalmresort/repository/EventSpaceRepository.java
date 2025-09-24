package com.sliit.goldenpalmresort.repository;

import com.sliit.goldenpalmresort.model.EventSpace;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface EventSpaceRepository extends JpaRepository<EventSpace, Long> {
    
    List<EventSpace> findByStatus(EventSpace.EventSpaceStatus status);
    
    List<EventSpace> findByIsActiveTrue();
    
    @Query("SELECT e FROM EventSpace e WHERE e.capacity >= :minCapacity AND e.capacity <= :maxCapacity")
    List<EventSpace> findByCapacityRange(@Param("minCapacity") Integer minCapacity, 
                                       @Param("maxCapacity") Integer maxCapacity);
    
    @Query("SELECT e FROM EventSpace e WHERE e.setupTypes LIKE %:eventType%")
    List<EventSpace> findByEventType(@Param("eventType") String eventType);
    
    @Query("SELECT e FROM EventSpace e WHERE e.id NOT IN " +
           "(SELECT DISTINCT eb.eventSpace.id FROM EventBooking eb WHERE " +
           "eb.eventDate = :eventDate AND " +
           "((eb.startTime <= :startTime AND eb.endTime > :startTime) OR " +
           "(eb.startTime < :endTime AND eb.endTime >= :endTime) OR " +
           "(eb.startTime >= :startTime AND eb.endTime <= :endTime)) AND " +
           "eb.status IN ('CONFIRMED', 'IN_PROGRESS')) AND " +
           "e.status = 'AVAILABLE' AND e.capacity >= :expectedGuests")
    List<EventSpace> findAvailableEventSpaces(@Param("eventDate") LocalDate eventDate,
                                            @Param("startTime") String startTime,
                                            @Param("endTime") String endTime,
                                            @Param("expectedGuests") Integer expectedGuests);
    
    @Query("SELECT e FROM EventSpace e WHERE e.cateringAvailable = true")
    List<EventSpace> findEventSpacesWithCatering();
    
    @Query("SELECT e FROM EventSpace e WHERE e.audioVisualEquipment = true")
    List<EventSpace> findEventSpacesWithAudioVisual();
    
    EventSpace findByName(String name);
} 