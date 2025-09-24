package com.sliit.goldenpalmresort.controller;

import com.sliit.goldenpalmresort.dto.BookingRequest;
import com.sliit.goldenpalmresort.dto.BookingResponse;
import com.sliit.goldenpalmresort.model.Booking.BookingStatus;
import com.sliit.goldenpalmresort.model.Room;
import com.sliit.goldenpalmresort.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@CrossOrigin(origins = "*")
public class BookingController {
    
    @Autowired
    private BookingService bookingService;
    
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@RequestBody BookingRequest request, Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String username = authentication.getName();
            BookingResponse response = bookingService.createBooking(request, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/user")
    public ResponseEntity<List<BookingResponse>> getUserBookings(Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String username = authentication.getName();
            List<BookingResponse> bookings = bookingService.getUserBookings(username);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/status/{status}")
    public ResponseEntity<List<BookingResponse>> getBookingsByStatus(@PathVariable BookingStatus status) {
        try {
            List<BookingResponse> bookings = bookingService.getBookingsByStatus(status);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/date-range")
    public ResponseEntity<List<BookingResponse>> getBookingsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<BookingResponse> bookings = bookingService.getBookingsByDateRange(startDate, endDate);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/status-date-range")
    public ResponseEntity<List<BookingResponse>> getBookingsByStatusAndDateRange(
            @RequestParam BookingStatus status,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<BookingResponse> bookings = bookingService.getBookingsByStatusAndDateRange(status, startDate, endDate);
            return ResponseEntity.ok(bookings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/available-rooms")
    public ResponseEntity<List<Room>> getAvailableRooms(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam Integer guestCount) {
        try {
            List<Room> availableRooms = bookingService.getAvailableRooms(checkIn, checkOut, guestCount);
            return ResponseEntity.ok(availableRooms);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{bookingReference}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable String bookingReference, Authentication authentication) {
        try {
            String username = authentication.getName();
            bookingService.cancelBooking(bookingReference, username);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}