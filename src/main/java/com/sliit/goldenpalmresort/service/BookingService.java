package com.sliit.goldenpalmresort.service;

import com.sliit.goldenpalmresort.dto.BookingRequest;
import com.sliit.goldenpalmresort.dto.BookingResponse;
import com.sliit.goldenpalmresort.exception.ResourceNotFoundException;
import com.sliit.goldenpalmresort.model.Booking;
import com.sliit.goldenpalmresort.model.Room;
import com.sliit.goldenpalmresort.model.User;
import com.sliit.goldenpalmresort.model.Booking.BookingStatus;
import com.sliit.goldenpalmresort.repository.BookingRepository;
import com.sliit.goldenpalmresort.repository.RoomRepository;
import com.sliit.goldenpalmresort.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingService {
    
    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;

    public BookingService(BookingRepository bookingRepository, RoomRepository roomRepository, UserRepository userRepository) {
        this.bookingRepository = bookingRepository;
        this.roomRepository = roomRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public BookingResponse createBooking(BookingRequest request, String username) {
        // Validate request
        validateBookingRequest(request);
        
        // Get current user (the person making the booking, could be admin/front desk)
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        // Use current user as the guest who owns this booking
        User guestUser = currentUser;
        
        // Get the room
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + request.getRoomId()));
        
        // Check room availability
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                request.getRoomId(), 
                request.getCheckInDate(), 
                request.getCheckOutDate()
        );
        
        if (!overlappingBookings.isEmpty()) {
            throw new IllegalStateException("Room is not available for the selected dates");
        }
        
        // Calculate total amount
        long numberOfNights = ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        BigDecimal totalAmount = room.getBasePrice().multiply(BigDecimal.valueOf(numberOfNights));
        
        // Create and save booking
        Booking booking = new Booking();
        booking.setUser(guestUser);
        booking.setRoom(room);
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setGuestCount(request.getGuestCount());
        booking.setTotalAmount(totalAmount);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setSpecialRequests(request.getSpecialRequests());
        booking.setBookingReference(generateBookingReference());
        booking.setCreatedBy(currentUser);
        
        Booking savedBooking = bookingRepository.save(booking);
        return BookingResponse.from(savedBooking);
    }
    
    public List<BookingResponse> getUserBookings(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        return bookingRepository.findByUser(user).stream()
                .map(BookingResponse::from)
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void cancelBooking(String bookingReference, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with reference: " + bookingReference));
        
        // Check if user is authorized to cancel this booking
        if (!booking.getUser().getId().equals(user.getId()) && !user.getRole().equals("ADMIN")) {
            throw new SecurityException("Not authorized to cancel this booking");
        }
        
        // Only allow cancellation if check-in is at least 24 hours away
        if (booking.getCheckInDate().isBefore(LocalDate.now().plusDays(1))) {
            throw new IllegalStateException("Cannot cancel booking within 24 hours of check-in");
        }
        
        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
    }
    
    public List<BookingResponse> getBookingsByStatus(BookingStatus status) {
        return bookingRepository.findByStatus(status).stream()
                .map(BookingResponse::from)
                .collect(Collectors.toList());
    }
    
    public List<BookingResponse> getBookingsByDateRange(LocalDate startDate, LocalDate endDate) {
        return bookingRepository.findByCheckInDateBetween(startDate, endDate).stream()
                .map(BookingResponse::from)
                .collect(Collectors.toList());
    }
    
    public List<BookingResponse> getBookingsByStatusAndDateRange(BookingStatus status, LocalDate startDate, LocalDate endDate) {
        return bookingRepository.findByStatusAndCheckInDateBetween(status, startDate, endDate).stream()
                .map(BookingResponse::from)
                .collect(Collectors.toList());
    }
    
    public List<Room> getAvailableRooms(LocalDate checkInDate, LocalDate checkOutDate, Integer guestCount) {
        return roomRepository.findAvailableRooms(checkInDate, checkOutDate, guestCount);
    }
    
    private void validateBookingRequest(BookingRequest request) {
        if (request.getCheckInDate() == null || request.getCheckOutDate() == null) {
            throw new IllegalArgumentException("Check-in and check-out dates are required");
        }
        
        if (request.getCheckOutDate().isBefore(request.getCheckInDate())) {
            throw new IllegalArgumentException("Check-out date must be after check-in date");
        }
        
        if (request.getGuestCount() == null || request.getGuestCount() < 1) {
            throw new IllegalArgumentException("At least one guest is required");
        }
        
        if (request.getRoomId() == null) {
            throw new IllegalArgumentException("Room ID is required");
        }
    }
    
    private String generateBookingReference() {
        return "BK" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}