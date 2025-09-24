package com.sliit.goldenpalmresort.repository;

import com.sliit.goldenpalmresort.model.EventBooking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventBookingRepository extends JpaRepository<EventBooking, Long> {
    
    Optional<EventBooking> findByBookingReference(String bookingReference);
    
    List<EventBooking> findByUserId(Long userId);
    
    @Query("SELECT eb FROM EventBooking eb WHERE eb.eventSpace.id = :eventSpaceId AND " +
           "eb.eventDate = :eventDate AND " +
           "((eb.startTime <= :startTime AND eb.endTime > :startTime) OR " +
           "(eb.startTime < :endTime AND eb.endTime >= :endTime) OR " +
           "(eb.startTime >= :startTime AND eb.endTime <= :endTime))")
    List<EventBooking> findConflictingEventBookings(@Param("eventSpaceId") Long eventSpaceId,
                                                   @Param("eventDate") LocalDate eventDate,
                                                   @Param("startTime") String startTime,
                                                   @Param("endTime") String endTime);
    
    @Query("SELECT eb FROM EventBooking eb WHERE eb.status = :status")
    List<EventBooking> findByStatus(@Param("status") EventBooking.EventBookingStatus status);
    
    @Query("SELECT eb FROM EventBooking eb WHERE eb.eventDate BETWEEN :startDate AND :endDate")
    List<EventBooking> findEventBookingsByDateRange(@Param("startDate") LocalDate startDate,
                                                   @Param("endDate") LocalDate endDate);
    
    @Query("SELECT eb FROM EventBooking eb WHERE eb.eventType = :eventType")
    List<EventBooking> findByEventType(@Param("eventType") String eventType);
    
    @Query("SELECT eb FROM EventBooking eb WHERE eb.eventDate = :eventDate")
    List<EventBooking> findByEventDate(@Param("eventDate") LocalDate eventDate);
} 