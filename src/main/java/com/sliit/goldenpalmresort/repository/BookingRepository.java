package com.sliit.goldenpalmresort.repository;

import com.sliit.goldenpalmresort.model.Booking;
import com.sliit.goldenpalmresort.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    Optional<Booking> findByBookingReference(String bookingReference);
    
    List<Booking> findByUser(User user);
    
    List<Booking> findByStatus(Booking.BookingStatus status);
    
    @Query("SELECT b FROM Booking b WHERE " +
           "b.room.id = :roomId AND " +
           "b.status <> 'CANCELLED' AND " +
           "((b.checkInDate <= :checkOutDate) AND (b.checkOutDate >= :checkInDate))")
    List<Booking> findOverlappingBookings(
            @Param("roomId") Long roomId,
            @Param("checkInDate") LocalDate checkInDate,
            @Param("checkOutDate") LocalDate checkOutDate);
            
    @Query("SELECT b FROM Booking b WHERE " +
           "b.checkInDate >= :startDate AND " +
           "b.checkInDate <= :endDate " +
           "ORDER BY b.checkInDate")
    List<Booking> findByCheckInDateBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
            
    @Query("SELECT b FROM Booking b WHERE " +
           "b.status = :status AND " +
           "b.checkInDate >= :startDate AND " +
           "b.checkInDate <= :endDate " +
           "ORDER BY b.checkInDate")
    List<Booking> findByStatusAndCheckInDateBetween(
            @Param("status") Booking.BookingStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}