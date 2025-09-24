package com.sliit.goldenpalmresort.service;

import com.sliit.goldenpalmresort.dto.EventBookingRequest;
import com.sliit.goldenpalmresort.dto.EventBookingResponse;
import com.sliit.goldenpalmresort.model.EventBooking;
import com.sliit.goldenpalmresort.model.EventSpace;
import com.sliit.goldenpalmresort.model.User;
import com.sliit.goldenpalmresort.repository.EventBookingRepository;
import com.sliit.goldenpalmresort.repository.EventSpaceRepository;
import com.sliit.goldenpalmresort.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class EventBookingService {
    
    @Autowired
    private EventBookingRepository eventBookingRepository;
    
    @Autowired
    private EventSpaceRepository eventSpaceRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    public EventBookingResponse createEventBooking(EventBookingRequest request, String username) {
        // Validate request
        validateEventBookingRequest(request);
        
        // Get current user
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check event space availability
        if (!isEventSpaceAvailable(request.getEventSpaceId(), request.getEventDate(), 
                                 request.getStartTime(), request.getEndTime())) {
            throw new RuntimeException("Event space is not available for selected date and time");
        }
        
        // Get event space
        EventSpace eventSpace = eventSpaceRepository.findById(request.getEventSpaceId())
                .orElseThrow(() -> new RuntimeException("Event space not found"));
        
        // Calculate total amount
        BigDecimal totalAmount = calculateEventTotalAmount(eventSpace, request.getStartTime(), 
                                                        request.getEndTime(), request.isCateringRequired(), 
                                                        request.isAudioVisualRequired());
        
        // Create event booking
        EventBooking eventBooking = new EventBooking();
        eventBooking.setUser(currentUser);
        eventBooking.setEventSpace(eventSpace);
        eventBooking.setEventType(request.getEventType());
        eventBooking.setEventDate(request.getEventDate());
        eventBooking.setStartTime(request.getStartTime());
        eventBooking.setEndTime(request.getEndTime());
        eventBooking.setExpectedGuests(request.getExpectedGuests());
        eventBooking.setTotalAmount(totalAmount);
        eventBooking.setStatus(EventBooking.EventBookingStatus.PENDING);
        eventBooking.setSetupRequirements(request.getSetupRequirements());
        eventBooking.setCateringRequired(request.isCateringRequired());
        eventBooking.setAudioVisualRequired(request.isAudioVisualRequired());
        eventBooking.setSpecialRequests(request.getSpecialRequests());
        eventBooking.setContactPerson(request.getContactPerson());
        eventBooking.setContactPhone(request.getContactPhone());
        eventBooking.setContactEmail(request.getContactEmail());
        
        // Save event booking
        eventBooking = eventBookingRepository.save(eventBooking);
        
        return EventBookingResponse.from(eventBooking);
    }
    
    public List<EventBookingResponse> getUserEventBookings(String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<EventBooking> eventBookings = eventBookingRepository.findByUserId(currentUser.getId());
        return eventBookings.stream()
                .map(EventBookingResponse::from)
                .collect(Collectors.toList());
    }
    
    public EventBookingResponse getEventBookingById(Long bookingId, String username) {
        EventBooking eventBooking = eventBookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Event booking not found"));
        
        // Check if user owns this booking or is admin
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!eventBooking.getUser().getId().equals(currentUser.getId()) && 
            !currentUser.getRole().equals(User.UserRole.ADMIN)) {
            throw new RuntimeException("Access denied");
        }
        
        return EventBookingResponse.from(eventBooking);
    }
    
    public EventBookingResponse cancelEventBooking(Long bookingId, String username) {
        EventBooking eventBooking = eventBookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("Event booking not found"));
        
        // Check if user owns this booking or is admin
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!eventBooking.getUser().getId().equals(currentUser.getId()) && 
            !currentUser.getRole().equals(User.UserRole.ADMIN)) {
            throw new RuntimeException("Access denied");
        }
        
        if (eventBooking.getStatus() != EventBooking.EventBookingStatus.PENDING && 
            eventBooking.getStatus() != EventBooking.EventBookingStatus.CONFIRMED) {
            throw new RuntimeException("Event booking cannot be cancelled");
        }
        
        eventBooking.setStatus(EventBooking.EventBookingStatus.CANCELLED);
        eventBooking = eventBookingRepository.save(eventBooking);
        
        return EventBookingResponse.from(eventBooking);
    }
    
    public List<EventSpace> getAvailableEventSpaces(LocalDate eventDate, String startTime, 
                                                   String endTime, Integer expectedGuests) {
        return eventSpaceRepository.findAvailableEventSpaces(eventDate, startTime, endTime, expectedGuests);
    }
    
    public List<EventSpace> getEventSpacesByEventType(String eventType) {
        return eventSpaceRepository.findByEventType(eventType);
    }
    
    public List<EventSpace> getEventSpacesWithCatering() {
        return eventSpaceRepository.findEventSpacesWithCatering();
    }
    
    public List<EventSpace> getEventSpacesWithAudioVisual() {
        return eventSpaceRepository.findEventSpacesWithAudioVisual();
    }
    
    private void validateEventBookingRequest(EventBookingRequest request) {
        if (request.getEventDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Event date cannot be in the past");
        }
        
        if (!request.isEndTimeAfterStartTime()) {
            throw new RuntimeException("End time must be after start time");
        }
        
        if (request.getExpectedGuests() < 1 || request.getExpectedGuests() > 1000) {
            throw new RuntimeException("Expected guests must be between 1 and 1000");
        }
        
        // Validate time format
        try {
            LocalTime.parse(request.getStartTime(), DateTimeFormatter.ofPattern("HH:mm"));
            LocalTime.parse(request.getEndTime(), DateTimeFormatter.ofPattern("HH:mm"));
        } catch (Exception e) {
            throw new RuntimeException("Invalid time format. Use HH:MM format (e.g., 14:30)");
        }
    }
    
    private boolean isEventSpaceAvailable(Long eventSpaceId, LocalDate eventDate, 
                                        String startTime, String endTime) {
        List<EventBooking> conflictingBookings = eventBookingRepository.findConflictingEventBookings(
            eventSpaceId, eventDate, startTime, endTime);
        return conflictingBookings.isEmpty();
    }
    
    private BigDecimal calculateEventTotalAmount(EventSpace eventSpace, String startTime, 
                                              String endTime, boolean cateringRequired, 
                                              boolean audioVisualRequired) {
        // Calculate hours
        LocalTime start = LocalTime.parse(startTime);
        LocalTime end = LocalTime.parse(endTime);
        int hours = end.getHour() - start.getHour();
        if (hours <= 0) hours += 24; // Handle overnight events
        
        // Base amount
        BigDecimal baseAmount = eventSpace.getBasePrice().multiply(BigDecimal.valueOf(hours));
        
        // Add catering cost if required and available
        if (cateringRequired && eventSpace.isCateringAvailable()) {
            baseAmount = baseAmount.add(BigDecimal.valueOf(50000)); // LKR 50,000 for catering
        }
        
        // Add audio-visual cost if required and available
        if (audioVisualRequired && eventSpace.isAudioVisualEquipment()) {
            baseAmount = baseAmount.add(BigDecimal.valueOf(25000)); // LKR 25,000 for A/V
        }
        
        return baseAmount;
    }
} 