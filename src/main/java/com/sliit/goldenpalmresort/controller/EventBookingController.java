package com.sliit.goldenpalmresort.controller;

import com.sliit.goldenpalmresort.dto.EventBookingRequest;
import com.sliit.goldenpalmresort.dto.EventBookingResponse;
import com.sliit.goldenpalmresort.model.EventSpace;
import com.sliit.goldenpalmresort.service.EventBookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/event-bookings")
@CrossOrigin(origins = "*")
public class EventBookingController {
    
    @Autowired
    private EventBookingService eventBookingService;
    
    @PostMapping
    public ResponseEntity<EventBookingResponse> createEventBooking(@RequestBody EventBookingRequest request, Authentication authentication) {
        try {
            // Use authenticated user
            String username = authentication != null ? authentication.getName() : null;
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            EventBookingResponse response = eventBookingService.createEventBooking(request, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    
    @GetMapping
    public ResponseEntity<List<EventBookingResponse>> getUserEventBookings(Authentication authentication) {
        try {
            String username = authentication != null ? authentication.getName() : null;
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            List<EventBookingResponse> eventBookings = eventBookingService.getUserEventBookings(username);
            return ResponseEntity.ok(eventBookings);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{bookingId}")
    public ResponseEntity<EventBookingResponse> getEventBookingById(@PathVariable Long bookingId, Authentication authentication) {
        try {
            String username = authentication != null ? authentication.getName() : null;
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            EventBookingResponse eventBooking = eventBookingService.getEventBookingById(bookingId, username);
            return ResponseEntity.ok(eventBooking);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
    
    @DeleteMapping("/{bookingId}")
    public ResponseEntity<EventBookingResponse> cancelEventBooking(@PathVariable Long bookingId, Authentication authentication) {
        try {
            String username = authentication != null ? authentication.getName() : null;
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            EventBookingResponse eventBooking = eventBookingService.cancelEventBooking(bookingId, username);
            return ResponseEntity.ok(eventBooking);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/available-event-spaces")
    public ResponseEntity<List<EventSpace>> getAvailableEventSpaces(
            @RequestParam LocalDate eventDate,
            @RequestParam String startTime,
            @RequestParam String endTime,
            @RequestParam Integer expectedGuests) {
        try {
            List<EventSpace> eventSpaces = eventBookingService.getAvailableEventSpaces(
                eventDate, startTime, endTime, expectedGuests);
            return ResponseEntity.ok(eventSpaces);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/event-spaces/type/{eventType}")
    public ResponseEntity<List<EventSpace>> getEventSpacesByEventType(@PathVariable String eventType) {
        try {
            List<EventSpace> eventSpaces = eventBookingService.getEventSpacesByEventType(eventType);
            return ResponseEntity.ok(eventSpaces);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/event-spaces/catering")
    public ResponseEntity<List<EventSpace>> getEventSpacesWithCatering() {
        try {
            List<EventSpace> eventSpaces = eventBookingService.getEventSpacesWithCatering();
            return ResponseEntity.ok(eventSpaces);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/event-spaces/audio-visual")
    public ResponseEntity<List<EventSpace>> getEventSpacesWithAudioVisual() {
        try {
            List<EventSpace> eventSpaces = eventBookingService.getEventSpacesWithAudioVisual();
            return ResponseEntity.ok(eventSpaces);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
} 