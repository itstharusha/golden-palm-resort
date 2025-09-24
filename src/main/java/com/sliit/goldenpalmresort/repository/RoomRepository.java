package com.sliit.goldenpalmresort.repository;

import com.sliit.goldenpalmresort.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    
    List<Room> findByStatus(Room.RoomStatus status);
    
    List<Room> findByRoomType(String roomType);
    
    List<Room> findByIsActiveTrue();
    
    @Query("SELECT r FROM Room r WHERE r.status = 'AVAILABLE' AND r.capacity >= :guestCount")
    List<Room> findAvailableRoomsByCapacity(@Param("guestCount") Integer guestCount);
    
    @Query("SELECT r FROM Room r WHERE r.id NOT IN " +
           "(SELECT DISTINCT b.room.id FROM Booking b WHERE " +
           "((b.checkInDate <= :checkInDate AND b.checkOutDate > :checkInDate) OR " +
           "(b.checkInDate < :checkOutDate AND b.checkOutDate >= :checkOutDate) OR " +
           "(b.checkInDate >= :checkInDate AND b.checkOutDate <= :checkOutDate)) AND " +
           "b.status IN ('CONFIRMED', 'CHECKED_IN')) AND " +
           "r.status = 'AVAILABLE' AND r.capacity >= :guestCount")
    List<Room> findAvailableRooms(@Param("checkInDate") LocalDate checkInDate,
                                 @Param("checkOutDate") LocalDate checkOutDate,
                                 @Param("guestCount") Integer guestCount);
    
    Room findByRoomNumber(String roomNumber);
    
    /**
     * Find all rooms that can accommodate at least the specified number of guests
     * @param capacity The minimum number of guests the room should accommodate
     * @return List of rooms that can accommodate the specified number of guests
     */
    List<Room> findByCapacityGreaterThanEqual(Integer capacity);
}