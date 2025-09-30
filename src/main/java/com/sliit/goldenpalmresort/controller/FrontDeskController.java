package com.sliit.goldenpalmresort.controller;

import com.sliit.goldenpalmresort.model.Booking;
import com.sliit.goldenpalmresort.model.EventBooking;
import com.sliit.goldenpalmresort.model.EventSpace;
import com.sliit.goldenpalmresort.model.Payment;
import com.sliit.goldenpalmresort.model.Room;
import com.sliit.goldenpalmresort.model.User;
import com.sliit.goldenpalmresort.repository.BookingRepository;
import com.sliit.goldenpalmresort.repository.EventBookingRepository;
import com.sliit.goldenpalmresort.repository.PaymentRepository;
import com.sliit.goldenpalmresort.repository.RoomRepository;
import com.sliit.goldenpalmresort.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/frontdesk")
@CrossOrigin(origins = "*")
public class FrontDeskController {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EventBookingRepository eventBookingRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    // Get front desk statistics
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            LocalDate today = LocalDate.now();

            // Today's check-ins
            long todayCheckins = bookingRepository.findAll().stream()
                    .filter(booking -> booking.getCheckInDate().equals(today) && 
                                     booking.getStatus() == Booking.BookingStatus.CHECKED_IN)
                    .count();
            stats.put("todayCheckins", todayCheckins);

            // Today's check-outs
            long todayCheckouts = bookingRepository.findAll().stream()
                    .filter(booking -> booking.getCheckOutDate().equals(today) && 
                                     booking.getStatus() == Booking.BookingStatus.CHECKED_OUT)
                    .count();
            stats.put("todayCheckouts", todayCheckouts);

            // Pending bookings
            long pendingBookings = bookingRepository.findByStatus(Booking.BookingStatus.PENDING).size() +
                                 eventBookingRepository.findByStatus(EventBooking.EventBookingStatus.PENDING).size();
            stats.put("pendingBookings", pendingBookings);

            // Current guests (checked in but not checked out)
            long currentGuests = bookingRepository.findAll().stream()
                    .filter(booking -> booking.getStatus() == Booking.BookingStatus.CHECKED_IN)
                    .count();
            stats.put("currentGuests", currentGuests);

            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Get today's arrivals
    @GetMapping("/today-arrivals")
    public ResponseEntity<List<Map<String, Object>>> getTodayArrivals() {
        try {
            LocalDate today = LocalDate.now();
            List<Booking> todayBookings = bookingRepository.findAll().stream()
                    .filter(booking -> booking.getCheckInDate().equals(today))
                    .toList();

            List<Map<String, Object>> arrivals = todayBookings.stream()
                    .map(booking -> {
                        Map<String, Object> arrival = new HashMap<>();
                        arrival.put("bookingReference", booking.getBookingReference());
                        arrival.put("guestName", booking.getUser().getFirstName() + " " + booking.getUser().getLastName());
                        arrival.put("roomNumber", booking.getRoom().getRoomNumber());
                        arrival.put("checkInDate", booking.getCheckInDate().toString());
                        arrival.put("status", booking.getStatus().name());
                        return arrival;
                    })
                    .toList();

            return ResponseEntity.ok(arrivals);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Get check-ins with improved error handling and validation
    @GetMapping("/checkins")
    public ResponseEntity<List<Map<String, Object>>> getCheckIns() {
        try {
            System.out.println("Getting check-ins...");
            
            // Use direct status query for better performance
            List<Booking> checkins = bookingRepository.findByStatus(Booking.BookingStatus.CHECKED_IN);
            System.out.println("Found " + checkins.size() + " check-ins");

            List<Map<String, Object>> checkinsData = new ArrayList<>();
            
            for (Booking booking : checkins) {
                try {
                    // Validate booking data
                    if (booking == null || booking.getBookingReference() == null) {
                        System.err.println("Skipping invalid booking");
                        continue;
                    }
                    
                    Map<String, Object> checkin = new HashMap<>();
                    checkin.put("bookingReference", booking.getBookingReference());
                    
                    // Safely access user data with validation
                    User user = booking.getUser();
                    if (user != null && user.getFirstName() != null && user.getLastName() != null) {
                        checkin.put("guestName", user.getFirstName() + " " + user.getLastName());
                        checkin.put("guestEmail", user.getEmail() != null ? user.getEmail() : "");
                        checkin.put("guestPhone", user.getPhone() != null ? user.getPhone() : "");
                    } else {
                        checkin.put("guestName", "Unknown Guest");
                        checkin.put("guestEmail", "");
                        checkin.put("guestPhone", "");
                    }
                    
                    // Safely access room data with validation
                    Room room = booking.getRoom();
                    if (room != null && room.getRoomNumber() != null) {
                        checkin.put("roomNumber", room.getRoomNumber());
                        checkin.put("roomType", room.getRoomType() != null ? room.getRoomType() : "");
                        checkin.put("floorNumber", room.getFloorNumber() != null ? room.getFloorNumber() : "");
                    } else {
                        checkin.put("roomNumber", "Unknown");
                        checkin.put("roomType", "");
                        checkin.put("floorNumber", "");
                    }
                    
                    // Add comprehensive booking data
                    checkin.put("checkInDate", booking.getCheckInDate() != null ? booking.getCheckInDate().toString() : "");
                    checkin.put("checkOutDate", booking.getCheckOutDate() != null ? booking.getCheckOutDate().toString() : "");
                    checkin.put("guestCount", booking.getGuestCount() != null ? booking.getGuestCount() : 0);
                    checkin.put("totalAmount", booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO);
                    checkin.put("specialRequests", booking.getSpecialRequests() != null ? booking.getSpecialRequests() : "");
                    checkin.put("status", booking.getStatus() != null ? booking.getStatus().name() : "UNKNOWN");
                    checkin.put("createdAt", booking.getCreatedAt() != null ? booking.getCreatedAt().toString() : "");
                    checkin.put("updatedAt", booking.getUpdatedAt() != null ? booking.getUpdatedAt().toString() : "");
                    
                    // Calculate stay duration
                    if (booking.getCheckInDate() != null && booking.getCheckOutDate() != null) {
                        long days = java.time.temporal.ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
                        checkin.put("stayDuration", days);
                    } else {
                        checkin.put("stayDuration", 0);
                    }
                    
                    checkinsData.add(checkin);
                } catch (Exception e) {
                    System.err.println("Error processing check-in booking " + (booking != null ? booking.getBookingReference() : "null") + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("Successfully processed " + checkinsData.size() + " check-ins");
            return ResponseEntity.ok(checkinsData);
        } catch (Exception e) {
            System.err.println("Error in getCheckIns: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(List.of());
        }
    }

    // Get check-outs with improved error handling and validation
    @GetMapping("/checkouts")
    public ResponseEntity<List<Map<String, Object>>> getCheckOuts() {
        try {
            System.out.println("Getting check-outs...");
            
            // Use direct status query for better performance
            List<Booking> checkouts = bookingRepository.findByStatus(Booking.BookingStatus.CHECKED_OUT);
            System.out.println("Found " + checkouts.size() + " check-outs");

            List<Map<String, Object>> checkoutsData = new ArrayList<>();
            
            for (Booking booking : checkouts) {
                try {
                    // Validate booking data
                    if (booking == null || booking.getBookingReference() == null) {
                        System.err.println("Skipping invalid booking");
                        continue;
                    }
                    
                    Map<String, Object> checkout = new HashMap<>();
                    checkout.put("bookingReference", booking.getBookingReference());
                    
                    // Safely access user data with validation
                    User user = booking.getUser();
                    if (user != null && user.getFirstName() != null && user.getLastName() != null) {
                        checkout.put("guestName", user.getFirstName() + " " + user.getLastName());
                        checkout.put("guestEmail", user.getEmail() != null ? user.getEmail() : "");
                        checkout.put("guestPhone", user.getPhone() != null ? user.getPhone() : "");
                    } else {
                        checkout.put("guestName", "Unknown Guest");
                        checkout.put("guestEmail", "");
                        checkout.put("guestPhone", "");
                    }
                    
                    // Safely access room data with validation
                    Room room = booking.getRoom();
                    if (room != null && room.getRoomNumber() != null) {
                        checkout.put("roomNumber", room.getRoomNumber());
                        checkout.put("roomType", room.getRoomType() != null ? room.getRoomType() : "");
                        checkout.put("floorNumber", room.getFloorNumber() != null ? room.getFloorNumber() : "");
                    } else {
                        checkout.put("roomNumber", "Unknown");
                        checkout.put("roomType", "");
                        checkout.put("floorNumber", "");
                    }
                    
                    // Add comprehensive booking data
                    checkout.put("checkInDate", booking.getCheckInDate() != null ? booking.getCheckInDate().toString() : "");
                    checkout.put("checkOutDate", booking.getCheckOutDate() != null ? booking.getCheckOutDate().toString() : "");
                    checkout.put("guestCount", booking.getGuestCount() != null ? booking.getGuestCount() : 0);
                    checkout.put("totalAmount", booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO);
                    checkout.put("specialRequests", booking.getSpecialRequests() != null ? booking.getSpecialRequests() : "");
                    checkout.put("status", booking.getStatus() != null ? booking.getStatus().name() : "UNKNOWN");
                    checkout.put("createdAt", booking.getCreatedAt() != null ? booking.getCreatedAt().toString() : "");
                    checkout.put("updatedAt", booking.getUpdatedAt() != null ? booking.getUpdatedAt().toString() : "");
                    
                    // Calculate stay duration
                    if (booking.getCheckInDate() != null && booking.getCheckOutDate() != null) {
                        long days = java.time.temporal.ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
                        checkout.put("stayDuration", days);
                    } else {
                        checkout.put("stayDuration", 0);
                    }
                    
                    checkoutsData.add(checkout);
                } catch (Exception e) {
                    System.err.println("Error processing check-out booking " + (booking != null ? booking.getBookingReference() : "null") + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("Successfully processed " + checkoutsData.size() + " check-outs");
            return ResponseEntity.ok(checkoutsData);
        } catch (Exception e) {
            System.err.println("Error in getCheckOuts: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(List.of());
        }
    }

    // Get pending bookings
    @GetMapping("/pending-bookings")
    public ResponseEntity<List<Map<String, Object>>> getPendingBookings() {
        try {
            System.out.println("Getting pending bookings...");
            
            // Get pending room bookings
            List<Booking> pendingRoomBookings = bookingRepository.findByStatus(Booking.BookingStatus.PENDING);
            System.out.println("Found " + pendingRoomBookings.size() + " pending room bookings");
            
            List<EventBooking> pendingEventBookings = eventBookingRepository.findByStatus(EventBooking.EventBookingStatus.PENDING);
            System.out.println("Found " + pendingEventBookings.size() + " pending event bookings");

            List<Map<String, Object>> pendingBookings = new ArrayList<>();
            
            // Process room bookings
            for (Booking booking : pendingRoomBookings) {
                try {
                    Map<String, Object> pending = new HashMap<>();
                    pending.put("bookingReference", booking.getBookingReference());
                    
                    // Safely access user data
                    User user = booking.getUser();
                    if (user != null) {
                        pending.put("guestName", user.getFirstName() + " " + user.getLastName());
                    } else {
                        pending.put("guestName", "Unknown");
                    }
                    
                    // Safely access room data
                    Room room = booking.getRoom();
                    if (room != null) {
                        pending.put("roomEvent", "Room " + room.getRoomNumber());
                    } else {
                        pending.put("roomEvent", "Unknown Room");
                    }
                    
                    pending.put("checkInDate", booking.getCheckInDate() != null ? booking.getCheckInDate().toString() : "");
                    pending.put("totalAmount", booking.getTotalAmount());
                    pending.put("status", booking.getStatus().name());
                    pending.put("type", "ROOM");
                    pendingBookings.add(pending);
                } catch (Exception e) {
                    System.err.println("Error processing pending room booking: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // Process event bookings
            for (EventBooking booking : pendingEventBookings) {
                try {
                    Map<String, Object> pending = new HashMap<>();
                    pending.put("bookingReference", booking.getBookingReference());
                    
                    // Safely access user data
                    User user = booking.getUser();
                    if (user != null) {
                        pending.put("guestName", user.getFirstName() + " " + user.getLastName());
                    } else {
                        pending.put("guestName", "Unknown");
                    }
                    
                    // Safely access event space data
                    EventSpace eventSpace = booking.getEventSpace();
                    if (eventSpace != null) {
                        pending.put("roomEvent", eventSpace.getName());
                    } else {
                        pending.put("roomEvent", "Unknown Event Space");
                    }
                    
                    pending.put("checkInDate", booking.getEventDate() != null ? booking.getEventDate().toString() : "");
                    pending.put("totalAmount", booking.getTotalAmount());
                    pending.put("status", booking.getStatus().name());
                    pending.put("type", "EVENT");
                    pendingBookings.add(pending);
                } catch (Exception e) {
                    System.err.println("Error processing pending event booking: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("Total pending bookings to return: " + pendingBookings.size());
            return ResponseEntity.ok(pendingBookings);
        } catch (Exception e) {
            System.err.println("Error in getPendingBookings: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // Get current guests with improved error handling and validation
    @GetMapping("/current-guests")
    public ResponseEntity<List<Map<String, Object>>> getCurrentGuests() {
        try {
            System.out.println("Getting current guests...");
            
            // Use direct status query for better performance
            List<Booking> currentGuests = bookingRepository.findByStatus(Booking.BookingStatus.CHECKED_IN);
            System.out.println("Found " + currentGuests.size() + " current guests");

            List<Map<String, Object>> guestsData = new ArrayList<>();
            
            for (Booking booking : currentGuests) {
                try {
                    // Validate booking data
                    if (booking == null || booking.getBookingReference() == null) {
                        System.err.println("Skipping invalid booking");
                        continue;
                    }
                    
                    Map<String, Object> guest = new HashMap<>();
                    guest.put("bookingReference", booking.getBookingReference());
                    
                    // Safely access user data with validation
                    User user = booking.getUser();
                    if (user != null && user.getFirstName() != null && user.getLastName() != null) {
                        guest.put("guestName", user.getFirstName() + " " + user.getLastName());
                        guest.put("guestEmail", user.getEmail() != null ? user.getEmail() : "");
                        guest.put("guestPhone", user.getPhone() != null ? user.getPhone() : "");
                    } else {
                        guest.put("guestName", "Unknown Guest");
                        guest.put("guestEmail", "");
                        guest.put("guestPhone", "");
                    }
                    
                    // Safely access room data with validation
                    Room room = booking.getRoom();
                    if (room != null && room.getRoomNumber() != null) {
                        guest.put("roomNumber", room.getRoomNumber());
                        guest.put("roomType", room.getRoomType() != null ? room.getRoomType() : "");
                        guest.put("floorNumber", room.getFloorNumber() != null ? room.getFloorNumber() : "");
                    } else {
                        guest.put("roomNumber", "Unknown");
                        guest.put("roomType", "");
                        guest.put("floorNumber", "");
                    }
                    
                    // Add comprehensive booking data
                    guest.put("checkInDate", booking.getCheckInDate() != null ? booking.getCheckInDate().toString() : "");
                    guest.put("checkOutDate", booking.getCheckOutDate() != null ? booking.getCheckOutDate().toString() : "");
                    guest.put("guestCount", booking.getGuestCount() != null ? booking.getGuestCount() : 0);
                    guest.put("totalAmount", booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO);
                    guest.put("specialRequests", booking.getSpecialRequests() != null ? booking.getSpecialRequests() : "");
                    guest.put("status", booking.getStatus() != null ? booking.getStatus().name() : "UNKNOWN");
                    guest.put("createdAt", booking.getCreatedAt() != null ? booking.getCreatedAt().toString() : "");
                    guest.put("updatedAt", booking.getUpdatedAt() != null ? booking.getUpdatedAt().toString() : "");
                    
                    // Calculate stay duration and remaining days
                    if (booking.getCheckInDate() != null && booking.getCheckOutDate() != null) {
                        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
                        long daysElapsed = java.time.temporal.ChronoUnit.DAYS.between(booking.getCheckInDate(), LocalDate.now());
                        long remainingDays = totalDays - daysElapsed;
                        
                        guest.put("stayDuration", totalDays);
                        guest.put("daysElapsed", daysElapsed);
                        guest.put("remainingDays", Math.max(0, remainingDays));
                    } else {
                        guest.put("stayDuration", 0);
                        guest.put("daysElapsed", 0);
                        guest.put("remainingDays", 0);
                    }
                    
                    guestsData.add(guest);
                } catch (Exception e) {
                    System.err.println("Error processing current guest booking " + (booking != null ? booking.getBookingReference() : "null") + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("Successfully processed " + guestsData.size() + " current guests");
            return ResponseEntity.ok(guestsData);
        } catch (Exception e) {
            System.err.println("Error in getCurrentGuests: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(List.of());
        }
    }

    // Process check-in with improved business logic and validation
    @PostMapping("/checkin")
    public ResponseEntity<?> processCheckIn(@RequestBody Map<String, Object> checkInData) {
        try {
            // Validate input data
            if (checkInData == null || !checkInData.containsKey("bookingReference")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Booking reference is required"));
            }
            
            String bookingReference = (String) checkInData.get("bookingReference");
            if (bookingReference == null || bookingReference.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Valid booking reference is required"));
            }
            
            System.out.println("Processing check-in for booking: " + bookingReference);
            
            Optional<Booking> bookingOpt = bookingRepository.findByBookingReference(bookingReference);
            if (bookingOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Booking not found with reference: " + bookingReference));
            }

            Booking booking = bookingOpt.get();
            
            // Validate booking status
            if (booking.getStatus() != Booking.BookingStatus.CONFIRMED) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "Booking must be confirmed before check-in. Current status: " + booking.getStatus()
                ));
            }
            
            // Validate check-in date
            LocalDate today = LocalDate.now();
            if (booking.getCheckInDate() != null && booking.getCheckInDate().isAfter(today)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "Cannot check in before the scheduled check-in date: " + booking.getCheckInDate()
                ));
            }
            
            // Validate room availability
            Room room = booking.getRoom();
            if (room == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Room information not found"));
            }
            
            if (room.getStatus() != com.sliit.goldenpalmresort.model.Room.RoomStatus.AVAILABLE) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "Room " + room.getRoomNumber() + " is not available for check-in. Status: " + room.getStatus()
                ));
            }
            
            // Validate guest information
            User user = booking.getUser();
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Guest information not found"));
            }
            
            // Process check-in
            try {
                // Update booking status to checked in
                booking.setStatus(Booking.BookingStatus.CHECKED_IN);
                booking.setUpdatedAt(LocalDateTime.now());
                
                // Update room status to occupied
                room.setStatus(com.sliit.goldenpalmresort.model.Room.RoomStatus.OCCUPIED);
                roomRepository.save(room);
                
                // Save booking
                bookingRepository.save(booking);
                
                // Complete any pending payments for this booking
                completeBookingPayments(booking);
                
                System.out.println("Successfully checked in guest: " + user.getFirstName() + " " + user.getLastName() + 
                                 " to room: " + room.getRoomNumber());
                
                return ResponseEntity.ok(Map.of(
                    "message", "Guest checked in successfully",
                    "bookingReference", booking.getBookingReference(),
                    "guestName", user.getFirstName() + " " + user.getLastName(),
                    "roomNumber", room.getRoomNumber(),
                    "checkInDate", today.toString(),
                    "status", booking.getStatus().name()
                ));
                
            } catch (Exception e) {
                System.err.println("Error during check-in process: " + e.getMessage());
                return ResponseEntity.internalServerError().body(Map.of("message", "Error during check-in process: " + e.getMessage()));
            }
            
        } catch (Exception e) {
            System.err.println("Error in processCheckIn: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("message", "Error processing check-in: " + e.getMessage()));
        }
    }

    // Process check-out with improved business logic and validation
    @PostMapping("/checkout")
    public ResponseEntity<?> processCheckOut(@RequestBody Map<String, Object> checkOutData) {
        try {
            // Validate input data
            if (checkOutData == null || !checkOutData.containsKey("bookingReference")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Booking reference is required"));
            }
            
            String bookingReference = (String) checkOutData.get("bookingReference");
            if (bookingReference == null || bookingReference.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Valid booking reference is required"));
            }
            
            System.out.println("Processing check-out for booking: " + bookingReference);
            
            Optional<Booking> bookingOpt = bookingRepository.findByBookingReference(bookingReference);
            if (bookingOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Booking not found with reference: " + bookingReference));
            }

            Booking booking = bookingOpt.get();
            
            // Validate booking status
            if (booking.getStatus() != Booking.BookingStatus.CHECKED_IN) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "Guest must be checked in before check-out. Current status: " + booking.getStatus()
                ));
            }
            
            // Validate check-out date
            LocalDate today = LocalDate.now();
            if (booking.getCheckOutDate() != null && booking.getCheckOutDate().isBefore(today)) {
                return ResponseEntity.badRequest().body(Map.of(
                    "message", "Cannot check out after the scheduled check-out date: " + booking.getCheckOutDate()
                ));
            }
            
            // Validate room information
            Room room = booking.getRoom();
            if (room == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Room information not found"));
            }
            
            // Validate guest information
            User user = booking.getUser();
            if (user == null) {
                return ResponseEntity.badRequest().body(Map.of("message", "Guest information not found"));
            }
            
            // Process check-out
            try {
                // Update booking status to checked out
                booking.setStatus(Booking.BookingStatus.CHECKED_OUT);
                booking.setUpdatedAt(LocalDateTime.now());
                
                // Update room status to available
                room.setStatus(com.sliit.goldenpalmresort.model.Room.RoomStatus.AVAILABLE);
                roomRepository.save(room);
                
                // Save booking
                bookingRepository.save(booking);
                
                // Complete any pending payments for this booking
                completeBookingPayments(booking);
                
                System.out.println("Successfully checked out guest: " + user.getFirstName() + " " + user.getLastName() + 
                                 " from room: " + room.getRoomNumber());
                
                return ResponseEntity.ok(Map.of(
                    "message", "Guest checked out successfully",
                    "bookingReference", booking.getBookingReference(),
                    "guestName", user.getFirstName() + " " + user.getLastName(),
                    "roomNumber", room.getRoomNumber(),
                    "checkOutDate", today.toString(),
                    "status", booking.getStatus().name()
                ));
                
            } catch (Exception e) {
                System.err.println("Error during check-out process: " + e.getMessage());
                return ResponseEntity.internalServerError().body(Map.of("message", "Error during check-out process: " + e.getMessage()));
            }
            
        } catch (Exception e) {
            System.err.println("Error in processCheckOut: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of("message", "Error processing check-out: " + e.getMessage()));
        }
    }

    // Confirm booking
    @PostMapping("/confirm-booking/{bookingReference}")
    public ResponseEntity<?> confirmBooking(@PathVariable String bookingReference) {
        try {
            // Try to find room booking first
            Optional<Booking> roomBookingOpt = bookingRepository.findByBookingReference(bookingReference);
            if (roomBookingOpt.isPresent()) {
                Booking booking = roomBookingOpt.get();
                booking.setStatus(Booking.BookingStatus.CONFIRMED);
                booking.setUpdatedAt(LocalDateTime.now());
                bookingRepository.save(booking);
                
                // Complete any pending payments for this booking
                completeBookingPayments(booking);
                return ResponseEntity.ok(Map.of("message", "Room booking confirmed successfully"));
            }

            // Try to find event booking
            Optional<EventBooking> eventBookingOpt = eventBookingRepository.findByBookingReference(bookingReference);
            if (eventBookingOpt.isPresent()) {
                EventBooking booking = eventBookingOpt.get();
                booking.setStatus(EventBooking.EventBookingStatus.CONFIRMED);
                booking.setUpdatedAt(LocalDateTime.now());
                eventBookingRepository.save(booking);
                return ResponseEntity.ok(Map.of("message", "Event booking confirmed successfully"));
            }

            return ResponseEntity.badRequest().body(Map.of("message", "Booking not found"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error confirming booking: " + e.getMessage()));
        }
    }

    // Reject booking
    @PostMapping("/reject-booking/{bookingReference}")
    public ResponseEntity<?> rejectBooking(@PathVariable String bookingReference) {
        try {
            // Try to find room booking first
            Optional<Booking> roomBookingOpt = bookingRepository.findByBookingReference(bookingReference);
            if (roomBookingOpt.isPresent()) {
                Booking booking = roomBookingOpt.get();
                booking.setStatus(Booking.BookingStatus.CANCELLED);
                booking.setUpdatedAt(LocalDateTime.now());
                
                // Update room status to available
                booking.getRoom().setStatus(com.sliit.goldenpalmresort.model.Room.RoomStatus.AVAILABLE);
                roomRepository.save(booking.getRoom());
                
                bookingRepository.save(booking);
                
                // Complete any pending payments for this booking
                completeBookingPayments(booking);
                return ResponseEntity.ok(Map.of("message", "Room booking rejected successfully"));
            }

            // Try to find event booking
            Optional<EventBooking> eventBookingOpt = eventBookingRepository.findByBookingReference(bookingReference);
            if (eventBookingOpt.isPresent()) {
                EventBooking booking = eventBookingOpt.get();
                booking.setStatus(EventBooking.EventBookingStatus.CANCELLED);
                booking.setUpdatedAt(LocalDateTime.now());
                eventBookingRepository.save(booking);
                return ResponseEntity.ok(Map.of("message", "Event booking rejected successfully"));
            }

            return ResponseEntity.badRequest().body(Map.of("message", "Booking not found"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error rejecting booking: " + e.getMessage()));
        }
    }

    // Get booking details by reference
    @GetMapping("/booking/{bookingReference}")
    public ResponseEntity<?> getBookingDetails(@PathVariable String bookingReference) {
        try {
            // Try to find room booking first
            Optional<Booking> roomBookingOpt = bookingRepository.findByBookingReference(bookingReference);
            if (roomBookingOpt.isPresent()) {
                Booking booking = roomBookingOpt.get();
                Map<String, Object> bookingDetails = new HashMap<>();
                bookingDetails.put("bookingReference", booking.getBookingReference());
                bookingDetails.put("guestName", booking.getUser().getFirstName() + " " + booking.getUser().getLastName());
                bookingDetails.put("guestEmail", booking.getUser().getEmail());
                bookingDetails.put("guestPhone", booking.getUser().getPhone());
                bookingDetails.put("roomNumber", booking.getRoom().getRoomNumber());
                bookingDetails.put("roomType", booking.getRoom().getRoomType());
                bookingDetails.put("checkInDate", booking.getCheckInDate().toString());
                bookingDetails.put("checkOutDate", booking.getCheckOutDate().toString());
                bookingDetails.put("guestCount", booking.getGuestCount());
                bookingDetails.put("totalAmount", booking.getTotalAmount());
                bookingDetails.put("specialRequests", booking.getSpecialRequests());
                bookingDetails.put("status", booking.getStatus().name());
                bookingDetails.put("createdAt", booking.getCreatedAt().toString());
                bookingDetails.put("updatedAt", booking.getUpdatedAt().toString());
                bookingDetails.put("type", "ROOM");
                return ResponseEntity.ok(bookingDetails);
            }

            // Try to find event booking
            Optional<EventBooking> eventBookingOpt = eventBookingRepository.findByBookingReference(bookingReference);
            if (eventBookingOpt.isPresent()) {
                EventBooking booking = eventBookingOpt.get();
                Map<String, Object> bookingDetails = new HashMap<>();
                bookingDetails.put("bookingReference", booking.getBookingReference());
                bookingDetails.put("guestName", booking.getUser().getFirstName() + " " + booking.getUser().getLastName());
                bookingDetails.put("guestEmail", booking.getUser().getEmail());
                bookingDetails.put("guestPhone", booking.getUser().getPhone());
                bookingDetails.put("eventSpaceName", booking.getEventSpace().getName());
                bookingDetails.put("eventDate", booking.getEventDate().toString());
                bookingDetails.put("eventTime", booking.getStartTime());
                bookingDetails.put("guestCount", booking.getExpectedGuests());
                bookingDetails.put("totalAmount", booking.getTotalAmount());
                bookingDetails.put("specialRequests", booking.getSpecialRequests());
                bookingDetails.put("status", booking.getStatus().name());
                bookingDetails.put("createdAt", booking.getCreatedAt().toString());
                bookingDetails.put("updatedAt", booking.getUpdatedAt().toString());
                bookingDetails.put("type", "EVENT");
                return ResponseEntity.ok(bookingDetails);
            }

            return ResponseEntity.badRequest().body(Map.of("message", "Booking not found"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error retrieving booking details: " + e.getMessage()));
        }
    }

    // Update booking details
    @PutMapping("/booking/{bookingReference}")
    public ResponseEntity<?> updateBooking(@PathVariable String bookingReference, @RequestBody Map<String, Object> updateData) {
        try {
            // Try to find room booking first
            Optional<Booking> roomBookingOpt = bookingRepository.findByBookingReference(bookingReference);
            if (roomBookingOpt.isPresent()) {
                Booking booking = roomBookingOpt.get();
                
                // Update allowed fields
                if (updateData.containsKey("guestCount")) {
                    booking.setGuestCount((Integer) updateData.get("guestCount"));
                }
                if (updateData.containsKey("specialRequests")) {
                    booking.setSpecialRequests((String) updateData.get("specialRequests"));
                }
                if (updateData.containsKey("checkInDate")) {
                    booking.setCheckInDate(LocalDate.parse((String) updateData.get("checkInDate")));
                }
                if (updateData.containsKey("checkOutDate")) {
                    booking.setCheckOutDate(LocalDate.parse((String) updateData.get("checkOutDate")));
                }
                if (updateData.containsKey("status")) {
                    booking.setStatus(Booking.BookingStatus.valueOf((String) updateData.get("status")));
                }
                
                booking.setUpdatedAt(LocalDateTime.now());
                bookingRepository.save(booking);
                
                // Complete any pending payments for this booking
                completeBookingPayments(booking);
                return ResponseEntity.ok(Map.of("message", "Room booking updated successfully"));
            }

            // Try to find event booking
            Optional<EventBooking> eventBookingOpt = eventBookingRepository.findByBookingReference(bookingReference);
            if (eventBookingOpt.isPresent()) {
                EventBooking booking = eventBookingOpt.get();
                
                // Update allowed fields
                if (updateData.containsKey("guestCount")) {
                    booking.setExpectedGuests((Integer) updateData.get("guestCount"));
                }
                if (updateData.containsKey("specialRequests")) {
                    booking.setSpecialRequests((String) updateData.get("specialRequests"));
                }
                if (updateData.containsKey("eventDate")) {
                    booking.setEventDate(LocalDate.parse((String) updateData.get("eventDate")));
                }
                if (updateData.containsKey("eventTime")) {
                    booking.setStartTime((String) updateData.get("eventTime"));
                }
                if (updateData.containsKey("status")) {
                    booking.setStatus(EventBooking.EventBookingStatus.valueOf((String) updateData.get("status")));
                }
                
                booking.setUpdatedAt(LocalDateTime.now());
                eventBookingRepository.save(booking);
                return ResponseEntity.ok(Map.of("message", "Event booking updated successfully"));
            }

            return ResponseEntity.badRequest().body(Map.of("message", "Booking not found"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error updating booking: " + e.getMessage()));
        }
    }

    // Get all bookings (for front desk view)
    @GetMapping("/all-bookings")
    public ResponseEntity<List<Map<String, Object>>> getAllBookings() {
        try {
            System.out.println("Getting all bookings...");
            
            // Get all room bookings with eager loading
            List<Booking> allRoomBookings = bookingRepository.findAll();
            System.out.println("Found " + allRoomBookings.size() + " room bookings");
            
            List<EventBooking> allEventBookings = eventBookingRepository.findAll();
            System.out.println("Found " + allEventBookings.size() + " event bookings");

            List<Map<String, Object>> allBookings = new ArrayList<>();
            
            // Process room bookings
            for (Booking booking : allRoomBookings) {
                try {
                    Map<String, Object> bookingData = new HashMap<>();
                    bookingData.put("bookingReference", booking.getBookingReference());
                    
                    // Safely access user data
                    User user = booking.getUser();
                    if (user != null) {
                        bookingData.put("guestName", user.getFirstName() + " " + user.getLastName());
                        bookingData.put("guestEmail", user.getEmail());
                    } else {
                        bookingData.put("guestName", "Unknown");
                        bookingData.put("guestEmail", "Unknown");
                    }
                    
                    // Safely access room data
                    Room room = booking.getRoom();
                    if (room != null) {
                        bookingData.put("roomEvent", "Room " + room.getRoomNumber());
                    } else {
                        bookingData.put("roomEvent", "Unknown Room");
                    }
                    
                    bookingData.put("checkInDate", booking.getCheckInDate() != null ? booking.getCheckInDate().toString() : "");
                    bookingData.put("checkOutDate", booking.getCheckOutDate() != null ? booking.getCheckOutDate().toString() : "");
                    bookingData.put("guestCount", booking.getGuestCount());
                    bookingData.put("totalAmount", booking.getTotalAmount());
                    bookingData.put("status", booking.getStatus().name());
                    bookingData.put("type", "ROOM");
                    bookingData.put("createdAt", booking.getCreatedAt() != null ? booking.getCreatedAt().toString() : "");
                    
                    allBookings.add(bookingData);
                } catch (Exception e) {
                    System.err.println("Error processing room booking: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            // Process event bookings
            for (EventBooking booking : allEventBookings) {
                try {
                    Map<String, Object> bookingData = new HashMap<>();
                    bookingData.put("bookingReference", booking.getBookingReference());
                    
                    // Safely access user data
                    User user = booking.getUser();
                    if (user != null) {
                        bookingData.put("guestName", user.getFirstName() + " " + user.getLastName());
                        bookingData.put("guestEmail", user.getEmail());
                    } else {
                        bookingData.put("guestName", "Unknown");
                        bookingData.put("guestEmail", "Unknown");
                    }
                    
                    // Safely access event space data
                    EventSpace eventSpace = booking.getEventSpace();
                    if (eventSpace != null) {
                        bookingData.put("roomEvent", eventSpace.getName());
                    } else {
                        bookingData.put("roomEvent", "Unknown Event Space");
                    }
                    
                    bookingData.put("eventDate", booking.getEventDate() != null ? booking.getEventDate().toString() : "");
                    bookingData.put("eventTime", booking.getStartTime());
                    bookingData.put("guestCount", booking.getExpectedGuests());
                    bookingData.put("totalAmount", booking.getTotalAmount());
                    bookingData.put("status", booking.getStatus().name());
                    bookingData.put("type", "EVENT");
                    bookingData.put("createdAt", booking.getCreatedAt() != null ? booking.getCreatedAt().toString() : "");
                    
                    allBookings.add(bookingData);
                } catch (Exception e) {
                    System.err.println("Error processing event booking: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            System.out.println("Total bookings to return: " + allBookings.size());
            return ResponseEntity.ok(allBookings);
        } catch (Exception e) {
            System.err.println("Error in getAllBookings: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Helper method to complete pending payments for a booking
    private void completeBookingPayments(Booking booking) {
        try {
            // Find all pending payments for this booking
            List<Payment> pendingPayments = paymentRepository.findAll().stream()
                .filter(payment -> payment.getBooking() != null && 
                                 payment.getBooking().getId().equals(booking.getId()) &&
                                 payment.getPaymentStatus() == Payment.PaymentStatus.PENDING)
                .toList();
            
            // Mark all pending payments as completed
            for (Payment payment : pendingPayments) {
                payment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);
                payment.setPaymentDate(LocalDateTime.now());
                payment.setProcessedBy("Front Desk - Auto-completed on check-in");
                paymentRepository.save(payment);
                System.out.println("Completed payment ID: " + payment.getId() + " for booking: " + booking.getBookingReference());
            }
            
            if (pendingPayments.isEmpty()) {
                System.out.println("No pending payments found for booking: " + booking.getBookingReference());
            } else {
                System.out.println("Completed " + pendingPayments.size() + " payments for booking: " + booking.getBookingReference());
            }
        } catch (Exception e) {
            System.err.println("Error completing payments for booking " + booking.getBookingReference() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
