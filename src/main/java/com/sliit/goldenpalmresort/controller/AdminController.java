package com.sliit.goldenpalmresort.controller;

import com.sliit.goldenpalmresort.dto.RegisterRequest;
import com.sliit.goldenpalmresort.dto.RoomUpdateRequest;
import com.sliit.goldenpalmresort.dto.EventSpaceUpdateRequest;
import com.sliit.goldenpalmresort.model.Booking;
import com.sliit.goldenpalmresort.model.EventSpace;
import com.sliit.goldenpalmresort.model.EventBooking;
import com.sliit.goldenpalmresort.model.Room;
import com.sliit.goldenpalmresort.model.User;
import com.sliit.goldenpalmresort.repository.BookingRepository;
import com.sliit.goldenpalmresort.repository.EventSpaceRepository;
import com.sliit.goldenpalmresort.repository.EventBookingRepository;
import com.sliit.goldenpalmresort.repository.RoomRepository;
import com.sliit.goldenpalmresort.repository.UserRepository;
import com.sliit.goldenpalmresort.repository.PaymentRepository;
import com.sliit.goldenpalmresort.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ArrayList;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private EventSpaceRepository eventSpaceRepository;

    @Autowired
    private EventBookingRepository eventBookingRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private PaymentRepository paymentRepository;

    // Get all users
    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = userRepository.findAll();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Get single user by ID
    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            
            if (userOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
            }
            
            return ResponseEntity.ok(userOpt.get());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error retrieving user: " + e.getMessage()));
        }
    }

                // Add new user
            @PostMapping("/users")
            public ResponseEntity<?> addUser(@RequestBody Map<String, Object> userData) {
                try {
                    String username = (String) userData.get("username");
                    String email = (String) userData.get("email");
                    String password = (String) userData.get("password");
                    String firstName = (String) userData.get("firstName");
                    String lastName = (String) userData.get("lastName");
                    String phone = (String) userData.get("phone");
                    String role = (String) userData.get("role");

                    // Check if username already exists
                    if (userRepository.findByUsername(username).isPresent()) {
                        return ResponseEntity.badRequest().body(Map.of("message", "Username already exists"));
                    }

                    // Check if email already exists
                    if (userRepository.findByEmail(email).isPresent()) {
                        return ResponseEntity.badRequest().body(Map.of("message", "Email already exists"));
                    }

                    // Create new user
                    User user = new User();
                    user.setUsername(username);
                    user.setEmail(email);
                    user.setPassword(passwordEncoder.encode(password));
                    user.setFirstName(firstName);
                    user.setLastName(lastName);
                    user.setPhone(phone);
                    user.setRole(User.UserRole.valueOf(role));
                    user.setActive(true);

                    user = userRepository.save(user);
                    return ResponseEntity.ok(user);
                } catch (Exception e) {
                    return ResponseEntity.internalServerError().body(Map.of("message", "Error creating user: " + e.getMessage()));
                }
            }

            // Update user role
            @PutMapping("/users/{userId}/role")
            public ResponseEntity<?> updateUserRole(@PathVariable Long userId, @RequestBody Map<String, String> roleData) {
                try {
                    String newRole = roleData.get("role");
                    Optional<User> userOpt = userRepository.findById(userId);
                    
                    if (userOpt.isEmpty()) {
                        return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
                    }

                    User user = userOpt.get();
                    user.setRole(User.UserRole.valueOf(newRole));
                    user = userRepository.save(user);
                    
                    return ResponseEntity.ok(user);
                } catch (Exception e) {
                    return ResponseEntity.internalServerError().body(Map.of("message", "Error updating user role: " + e.getMessage()));
                }
            }

            // Update user
            @PutMapping("/users/{userId}")
            public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody Map<String, Object> userData) {
                try {
                    Optional<User> userOpt = userRepository.findById(userId);
                    
                    if (userOpt.isEmpty()) {
                        return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
                    }

                    User user = userOpt.get();
                    
                    // Update basic fields
                    if (userData.containsKey("firstName")) {
                        user.setFirstName((String) userData.get("firstName"));
                    }
                    if (userData.containsKey("lastName")) {
                        user.setLastName((String) userData.get("lastName"));
                    }
                    if (userData.containsKey("username")) {
                        user.setUsername((String) userData.get("username"));
                    }
                    if (userData.containsKey("email")) {
                        user.setEmail((String) userData.get("email"));
                    }
                    if (userData.containsKey("phone")) {
                        user.setPhone((String) userData.get("phone"));
                    }
                    if (userData.containsKey("role")) {
                        user.setRole(User.UserRole.valueOf((String) userData.get("role")));
                    }
                    if (userData.containsKey("isActive")) {
                        user.setActive((Boolean) userData.get("isActive"));
                    }
                    
                    // Update password only if provided
                    if (userData.containsKey("password") && userData.get("password") != null && 
                        !((String) userData.get("password")).trim().isEmpty()) {
                        String password = (String) userData.get("password");
                        user.setPassword(passwordEncoder.encode(password));
                    }
                    
                    user = userRepository.save(user);
                    
                    return ResponseEntity.ok(Map.of("message", "User updated successfully"));
                } catch (Exception e) {
                    return ResponseEntity.internalServerError().body(Map.of("message", "Error updating user: " + e.getMessage()));
                }
            }

            // Delete user
            @DeleteMapping("/users/{userId}")
            public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
                try {
                    Optional<User> userOpt = userRepository.findById(userId);
                    
                    if (userOpt.isEmpty()) {
                        return ResponseEntity.badRequest().body(Map.of("message", "User not found"));
                    }

                    User user = userOpt.get();
                    userRepository.delete(user);
                    
                    return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
                } catch (Exception e) {
                    return ResponseEntity.internalServerError().body(Map.of("message", "Error deleting user: " + e.getMessage()));
                }
            }

            // Get user roles
            @GetMapping("/user-roles")
            public ResponseEntity<List<String>> getUserRoles() {
                try {
                    List<String> roles = List.of(
                        "ADMIN", "MANAGER", "FRONT_DESK", "PAYMENT_OFFICER", "GUEST"
                    );
                    return ResponseEntity.ok(roles);
                } catch (Exception e) {
                    return ResponseEntity.internalServerError().build();
                }
            }

    // Get statistics
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Total users
            long totalUsers = userRepository.count();
            stats.put("totalUsers", totalUsers);
            
            // Available rooms
            long availableRooms = roomRepository.findByStatus(Room.RoomStatus.AVAILABLE).size();
            stats.put("availableRooms", availableRooms);
            
            // Active bookings
            long activeBookings = bookingRepository.findByStatus(Booking.BookingStatus.CONFIRMED).size();
            stats.put("activeBookings", activeBookings);
            
            // Monthly revenue from actual payments
            BigDecimal monthlyRevenue = calculateMonthlyRevenue();
            stats.put("monthlyRevenue", monthlyRevenue);
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Get recent bookings
    @GetMapping("/recent-bookings")
    public ResponseEntity<List<Map<String, Object>>> getRecentBookings() {
        try {
            List<Map<String, Object>> roomBookings = bookingRepository.findAll().stream()
                    .map(booking -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("bookingReference", booking.getBookingReference());
                        m.put("guestName", booking.getUser().getFirstName() + " " + booking.getUser().getLastName());
                        m.put("type", "Room");
                        m.put("checkInDate", booking.getCheckInDate().toString());
                        m.put("checkOutDate", booking.getCheckOutDate().toString());
                        m.put("status", booking.getStatus().name());
                        m.put("totalAmount", booking.getTotalAmount());
                        m.put("createdAt", booking.getCreatedAt());
                        return m;
                    })
                    .toList();

            List<Map<String, Object>> eventBookings = eventBookingRepository.findAll().stream()
                    .map(ev -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("bookingReference", ev.getBookingReference());
                        m.put("guestName", ev.getUser().getFirstName() + " " + ev.getUser().getLastName());
                        m.put("type", "Event");
                        m.put("checkInDate", ev.getEventDate().toString());
                        m.put("checkOutDate", ev.getEventDate().toString());
                        m.put("status", ev.getStatus().name());
                        m.put("totalAmount", ev.getTotalAmount());
                        m.put("createdAt", ev.getCreatedAt());
                        return m;
                    })
                    .toList();

            List<Map<String, Object>> combined = new ArrayList<>();
            combined.addAll(roomBookings);
            combined.addAll(eventBookings);

            combined.sort((a,b) -> {
                java.time.LocalDateTime ca = (java.time.LocalDateTime) a.get("createdAt");
                java.time.LocalDateTime cb = (java.time.LocalDateTime) b.get("createdAt");
                if (ca == null && cb == null) return 0;
                if (ca == null) return 1;
                if (cb == null) return -1;
                return cb.compareTo(ca);
            });

            List<Map<String, Object>> top5 = combined.stream().limit(5).map(m -> {
                m.remove("createdAt");
                return m;
            }).toList();

            return ResponseEntity.ok(top5);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Get all bookings
    @GetMapping("/bookings")
    public ResponseEntity<List<Map<String, Object>>> getAllBookings() {
        try {
            List<Map<String, Object>> roomBookings = bookingRepository.findAll().stream()
                    .map(booking -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("bookingReference", booking.getBookingReference());
                        m.put("guestName", booking.getUser().getFirstName() + " " + booking.getUser().getLastName());
                        m.put("type", "Room");
                        m.put("checkInDate", booking.getCheckInDate().toString());
                        m.put("checkOutDate", booking.getCheckOutDate().toString());
                        m.put("status", booking.getStatus().name());
                        m.put("totalAmount", booking.getTotalAmount());
                        m.put("createdAt", booking.getCreatedAt());
                        return m;
                    })
                    .toList();

            List<Map<String, Object>> eventBookings = eventBookingRepository.findAll().stream()
                    .map(ev -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("bookingReference", ev.getBookingReference());
                        m.put("guestName", ev.getUser().getFirstName() + " " + ev.getUser().getLastName());
                        m.put("type", "Event");
                        m.put("checkInDate", ev.getEventDate().toString());
                        m.put("checkOutDate", ev.getEventDate().toString());
                        m.put("status", ev.getStatus().name());
                        m.put("totalAmount", ev.getTotalAmount());
                        m.put("createdAt", ev.getCreatedAt());
                        return m;
                    })
                    .toList();

            List<Map<String, Object>> combined = new ArrayList<>();
            combined.addAll(roomBookings);
            combined.addAll(eventBookings);

            combined.sort((a,b) -> {
                java.time.LocalDateTime ca = (java.time.LocalDateTime) a.get("createdAt");
                java.time.LocalDateTime cb = (java.time.LocalDateTime) b.get("createdAt");
                if (ca == null && cb == null) return 0;
                if (ca == null) return 1;
                if (cb == null) return -1;
                return cb.compareTo(ca);
            });

            // Remove createdAt before returning (frontend doesn't need it directly)
            List<Map<String, Object>> response = combined.stream().map(m -> {
                m.remove("createdAt");
                return m;
            }).toList();

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Delete booking by reference
    @DeleteMapping("/bookings/{bookingReference}")
    @Transactional
    public ResponseEntity<?> deleteBooking(@PathVariable String bookingReference, @RequestParam(value = "type", required = false) String type) {
        try {
            String ref = bookingReference != null ? bookingReference.trim() : "";
            String typeNorm = type != null ? type.trim().toLowerCase() : null;

            // If type is provided, prioritize it
            if ("event".equals(typeNorm)) {
                Optional<EventBooking> eventOpt = eventBookingRepository.findByBookingReference(ref);
                if (eventOpt.isPresent()) {
                    EventBooking ev = eventOpt.get();
                    List<com.sliit.goldenpalmresort.model.Payment> payments = paymentRepository.findByEventBookingId(ev.getId());
                    if (!payments.isEmpty()) paymentRepository.deleteAll(payments);
                    eventBookingRepository.delete(ev);
                    return ResponseEntity.ok(Map.of("message", "Event booking deleted successfully"));
                }
                return ResponseEntity.badRequest().body(Map.of("message", "Booking not found"));
            } else if ("room".equals(typeNorm)) {
                Optional<Booking> bookingOpt = bookingRepository.findByBookingReference(ref);
                if (bookingOpt.isPresent()) {
                    Booking booking = bookingOpt.get();
                    List<com.sliit.goldenpalmresort.model.Payment> payments = paymentRepository.findByBookingId(booking.getId());
                    if (!payments.isEmpty()) paymentRepository.deleteAll(payments);
                    bookingRepository.delete(booking);
                    return ResponseEntity.ok(Map.of("message", "Booking deleted successfully"));
                }
                return ResponseEntity.badRequest().body(Map.of("message", "Booking not found"));
            }

            Optional<Booking> bookingOpt = bookingRepository.findByBookingReference(ref);
            if (bookingOpt.isPresent()) {
                Booking booking = bookingOpt.get();
                // Delete dependent payments first to satisfy FK constraints
                List<com.sliit.goldenpalmresort.model.Payment> payments = paymentRepository.findByBookingId(booking.getId());
                if (!payments.isEmpty()) {
                    paymentRepository.deleteAll(payments);
                }
                bookingRepository.delete(booking);
                return ResponseEntity.ok(Map.of("message", "Booking deleted successfully"));
            }

            // Try event bookings if not a room booking
            Optional<EventBooking> eventOpt = eventBookingRepository.findByBookingReference(ref);
            if (eventOpt.isPresent()) {
                EventBooking ev = eventOpt.get();
                List<com.sliit.goldenpalmresort.model.Payment> payments = paymentRepository.findByEventBookingId(ev.getId());
                if (!payments.isEmpty()) {
                    paymentRepository.deleteAll(payments);
                }
                eventBookingRepository.delete(ev);
                return ResponseEntity.ok(Map.of("message", "Event booking deleted successfully"));
            }

            return ResponseEntity.badRequest().body(Map.of("message", "Booking not found"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error deleting booking: " + e.getMessage()));
        }
    }

    // Add new room
    @PostMapping("/rooms")
    public ResponseEntity<?> addRoom(@RequestBody Map<String, Object> roomData) {
        try {
            Room room = new Room();
            room.setRoomNumber((String) roomData.get("roomNumber"));
            room.setRoomType((String) roomData.get("roomType"));
            
            // Handle integer conversion properly
            Object floorNumberObj = roomData.get("floorNumber");
            if (floorNumberObj instanceof String) {
                room.setFloorNumber(Integer.parseInt((String) floorNumberObj));
            } else if (floorNumberObj instanceof Integer) {
                room.setFloorNumber((Integer) floorNumberObj);
            } else {
                room.setFloorNumber(1); // default value
            }
            
            Object capacityObj = roomData.get("capacity");
            if (capacityObj instanceof String) {
                room.setCapacity(Integer.parseInt((String) capacityObj));
            } else if (capacityObj instanceof Integer) {
                room.setCapacity((Integer) capacityObj);
            } else {
                room.setCapacity(2); // default value
            }
            
            // Handle BigDecimal conversion properly
            Object basePriceObj = roomData.get("basePrice");
            if (basePriceObj instanceof String) {
                room.setBasePrice(new BigDecimal((String) basePriceObj));
            } else if (basePriceObj instanceof Number) {
                room.setBasePrice(new BigDecimal(basePriceObj.toString()));
            } else {
                room.setBasePrice(new BigDecimal("100.00")); // default value
            }
            
            room.setDescription((String) roomData.get("description"));
            room.setAmenities((String) roomData.get("amenities"));
            room.setImageUrls((String) roomData.get("imageUrls"));
            room.setStatus(Room.RoomStatus.AVAILABLE);
            room.setActive(true);

            room = roomRepository.save(room);
            return ResponseEntity.ok(room);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error creating room: " + e.getMessage()));
        }
    }

    // Add new event space
    @PostMapping("/event-spaces")
    public ResponseEntity<?> addEventSpace(@RequestBody Map<String, Object> eventSpaceData) {
        try {
            EventSpace eventSpace = new EventSpace();
            eventSpace.setName((String) eventSpaceData.get("name"));
            
            // Handle integer conversion properly
            Object capacityObj = eventSpaceData.get("capacity");
            if (capacityObj instanceof String) {
                eventSpace.setCapacity(Integer.parseInt((String) capacityObj));
            } else if (capacityObj instanceof Integer) {
                eventSpace.setCapacity((Integer) capacityObj);
            } else {
                eventSpace.setCapacity(50); // default value
            }
            
            // Handle BigDecimal conversion properly
            Object basePriceObj = eventSpaceData.get("basePrice");
            if (basePriceObj instanceof String) {
                eventSpace.setBasePrice(new BigDecimal((String) basePriceObj));
            } else if (basePriceObj instanceof Number) {
                eventSpace.setBasePrice(new BigDecimal(basePriceObj.toString()));
            } else {
                eventSpace.setBasePrice(new BigDecimal("500.00")); // default value
            }
            
            eventSpace.setDescription((String) eventSpaceData.get("description"));
            eventSpace.setSetupTypes((String) eventSpaceData.get("setupTypes"));
            eventSpace.setAmenities((String) eventSpaceData.get("amenities"));
            
            // Handle floor number conversion
            Object floorNumberObj = eventSpaceData.get("floorNumber");
            if (floorNumberObj instanceof String) {
                eventSpace.setFloorNumber(Integer.parseInt((String) floorNumberObj));
            } else if (floorNumberObj instanceof Integer) {
                eventSpace.setFloorNumber((Integer) floorNumberObj);
            } else {
                eventSpace.setFloorNumber(1); // default value
            }
            
            eventSpace.setDimensions((String) eventSpaceData.get("dimensions"));
            eventSpace.setImageUrls((String) eventSpaceData.get("imageUrls"));
            eventSpace.setStatus(EventSpace.EventSpaceStatus.AVAILABLE);
            eventSpace.setActive(true);

            eventSpace = eventSpaceRepository.save(eventSpace);
            return ResponseEntity.ok(eventSpace);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error creating event space: " + e.getMessage()));
        }
    }
    
    // Update room details
    @PutMapping("/rooms/{roomId}")
    public ResponseEntity<?> updateRoom(@PathVariable Long roomId, @RequestBody RoomUpdateRequest request) {
        try {
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Room not found"));
            
            room.setRoomNumber(request.getRoomNumber());
            room.setRoomType(request.getRoomType());
            room.setFloorNumber(request.getFloorNumber());
            room.setBasePrice(request.getBasePrice());
            room.setCapacity(request.getCapacity());
            room.setDescription(request.getDescription());
            room.setAmenities(request.getAmenities());
            room.setImageUrls(request.getImageUrls());
            
            if (request.getStatus() != null) {
                room.setStatus(Room.RoomStatus.valueOf(request.getStatus()));
            }
            
            room = roomRepository.save(room);
            return ResponseEntity.ok(room);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error updating room: " + e.getMessage()));
        }
    }
    
    // Update event space details
    @PutMapping("/event-spaces/{eventSpaceId}")
    public ResponseEntity<?> updateEventSpace(@PathVariable Long eventSpaceId, @RequestBody EventSpaceUpdateRequest request) {
        try {
            EventSpace eventSpace = eventSpaceRepository.findById(eventSpaceId)
                    .orElseThrow(() -> new RuntimeException("Event space not found"));
            
            eventSpace.setName(request.getName());
            eventSpace.setDescription(request.getDescription());
            eventSpace.setCapacity(request.getCapacity());
            eventSpace.setBasePrice(request.getBasePrice());
            eventSpace.setSetupTypes(request.getSetupTypes());
            eventSpace.setAmenities(request.getAmenities());
            eventSpace.setFloorNumber(request.getFloorNumber());
            eventSpace.setDimensions(request.getDimensions());
            eventSpace.setCateringAvailable(request.getCateringAvailable());
            eventSpace.setAudioVisualEquipment(request.getAudioVisualEquipment());
            eventSpace.setParkingAvailable(request.getParkingAvailable());
            eventSpace.setImageUrls(request.getImageUrls());
            
            if (request.getStatus() != null) {
                eventSpace.setStatus(EventSpace.EventSpaceStatus.valueOf(request.getStatus()));
            }
            
            eventSpace = eventSpaceRepository.save(eventSpace);
            return ResponseEntity.ok(eventSpace);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error updating event space: " + e.getMessage()));
        }
    }
    
    // Delete room
    @DeleteMapping("/rooms/{roomId}")
    public ResponseEntity<?> deleteRoom(@PathVariable Long roomId) {
        try {
            System.out.println("Delete room request received for room ID: " + roomId);
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new RuntimeException("Room not found"));
            
            System.out.println("Room found: " + room.getRoomNumber());
            room.setActive(false);
            roomRepository.save(room);
            System.out.println("Room deleted successfully");
            return ResponseEntity.ok(Map.of("message", "Room deleted successfully"));
        } catch (Exception e) {
            System.out.println("Error deleting room: " + e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("message", "Error deleting room: " + e.getMessage()));
        }
    }
    
    // Delete event space
    @DeleteMapping("/event-spaces/{eventSpaceId}")
    public ResponseEntity<?> deleteEventSpace(@PathVariable Long eventSpaceId) {
        try {
            System.out.println("Delete event space request received for event space ID: " + eventSpaceId);
            EventSpace eventSpace = eventSpaceRepository.findById(eventSpaceId)
                    .orElseThrow(() -> new RuntimeException("Event space not found"));
            
            System.out.println("Event space found: " + eventSpace.getName());
            eventSpace.setActive(false);
            eventSpaceRepository.save(eventSpace);
            System.out.println("Event space deleted successfully");
            return ResponseEntity.ok(Map.of("message", "Event space deleted successfully"));
        } catch (Exception e) {
            System.out.println("Error deleting event space: " + e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of("message", "Error deleting event space: " + e.getMessage()));
        }
    }

    // ==================== DASHBOARD ANALYTICS ENDPOINTS ====================

    // Get revenue chart data
    @GetMapping("/analytics/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueAnalytics(
            @RequestParam(defaultValue = "month") String period) {
        try {
            Map<String, Object> revenueData = new HashMap<>();
            LocalDate now = LocalDate.now();
            
            List<String> labels = new ArrayList<>();
            List<Double> revenues = new ArrayList<>();
            
            int periodsToShow = 7; // Default for monthly view
            
            switch (period.toLowerCase()) {
                case "week":
                    // Last 7 days
                    periodsToShow = 7;
                    for (int i = 6; i >= 0; i--) {
                        LocalDate day = now.minusDays(i);
                        labels.add(day.format(java.time.format.DateTimeFormatter.ofPattern("EEE")));
                        
                        LocalDateTime dayStart = day.atStartOfDay();
                        LocalDateTime dayEnd = day.atTime(23, 59, 59);
                        
                        double dailyRevenue = paymentRepository.findAll().stream()
                            .filter(p -> p.getPaymentStatus() == com.sliit.goldenpalmresort.model.Payment.PaymentStatus.COMPLETED)
                            .filter(p -> p.getPaymentDate() != null)
                            .filter(p -> !p.getPaymentDate().isBefore(dayStart) && !p.getPaymentDate().isAfter(dayEnd))
                            .mapToDouble(p -> p.getAmount().doubleValue())
                            .sum();
                        
                        revenues.add(Math.round(dailyRevenue * 100.0) / 100.0);
                    }
                    break;
                    
                case "year":
                    // Last 12 months
                    periodsToShow = 12;
                    for (int i = 11; i >= 0; i--) {
                        LocalDate month = now.minusMonths(i);
                        labels.add(month.format(java.time.format.DateTimeFormatter.ofPattern("MMM yyyy")));
                        
                        LocalDate monthStart = month.withDayOfMonth(1);
                        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
                        LocalDateTime startDateTime = monthStart.atStartOfDay();
                        LocalDateTime endDateTime = monthEnd.atTime(23, 59, 59);
                        
                        double monthlyRevenue = paymentRepository.findAll().stream()
                            .filter(p -> p.getPaymentStatus() == com.sliit.goldenpalmresort.model.Payment.PaymentStatus.COMPLETED)
                            .filter(p -> p.getPaymentDate() != null)
                            .filter(p -> !p.getPaymentDate().isBefore(startDateTime) && !p.getPaymentDate().isAfter(endDateTime))
                            .mapToDouble(p -> p.getAmount().doubleValue())
                            .sum();
                        
                        revenues.add(Math.round(monthlyRevenue * 100.0) / 100.0);
                    }
                    break;
                    
                default: // "month" - Last 7 months
                    periodsToShow = 7;
                    for (int i = 6; i >= 0; i--) {
                        LocalDate month = now.minusMonths(i);
                        labels.add(month.format(java.time.format.DateTimeFormatter.ofPattern("MMM")));
                        
                        LocalDate monthStart = month.withDayOfMonth(1);
                        LocalDate monthEnd = monthStart.plusMonths(1).minusDays(1);
                        LocalDateTime startDateTime = monthStart.atStartOfDay();
                        LocalDateTime endDateTime = monthEnd.atTime(23, 59, 59);
                        
                        double monthlyRevenue = paymentRepository.findAll().stream()
                            .filter(p -> p.getPaymentStatus() == com.sliit.goldenpalmresort.model.Payment.PaymentStatus.COMPLETED)
                            .filter(p -> p.getPaymentDate() != null)
                            .filter(p -> !p.getPaymentDate().isBefore(startDateTime) && !p.getPaymentDate().isAfter(endDateTime))
                            .mapToDouble(p -> p.getAmount().doubleValue())
                            .sum();
                        
                        revenues.add(Math.round(monthlyRevenue * 100.0) / 100.0);
                    }
            }
            
            // Calculate growth (current vs previous period)
            double currentRevenue = revenues.isEmpty() ? 0 : revenues.get(revenues.size() - 1);
            double previousRevenue = revenues.size() > 1 ? revenues.get(revenues.size() - 2) : 0;
            double growthPercentage = previousRevenue > 0 ? 
                ((currentRevenue - previousRevenue) / previousRevenue) * 100 : 
                (currentRevenue > 0 ? 100 : 0);
            
            // Calculate total revenue across all periods
            double totalRevenue = revenues.stream().mapToDouble(Double::doubleValue).sum();
            
            // Calculate average revenue
            double averageRevenue = revenues.isEmpty() ? 0 : totalRevenue / revenues.size();
            
            revenueData.put("labels", labels);
            revenueData.put("data", revenues);
            revenueData.put("currentPeriod", Math.round(currentRevenue));
            revenueData.put("totalRevenue", Math.round(totalRevenue));
            revenueData.put("averageRevenue", Math.round(averageRevenue));
            revenueData.put("growthPercentage", Math.round(growthPercentage * 100.0) / 100.0);
            revenueData.put("isPositiveGrowth", growthPercentage > 0);
            revenueData.put("period", period);
            revenueData.put("hasData", !revenues.isEmpty() && totalRevenue > 0);
            
            return ResponseEntity.ok(revenueData);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    // Get room status analytics
    @GetMapping("/analytics/rooms")
    public ResponseEntity<Map<String, Object>> getRoomAnalytics() {
        try {
            Map<String, Object> roomData = new HashMap<>();
            
            List<Room> allRooms = roomRepository.findAll();
            
            // Count rooms by status
            long availableRooms = allRooms.stream().filter(room -> room.getStatus() == Room.RoomStatus.AVAILABLE).count();
            long occupiedRooms = allRooms.stream().filter(room -> room.getStatus() == Room.RoomStatus.OCCUPIED).count();
            long maintenanceRooms = allRooms.stream().filter(room -> room.getStatus() == Room.RoomStatus.MAINTENANCE).count();
            long outOfOrderRooms = allRooms.stream().filter(room -> room.getStatus() == Room.RoomStatus.BLOCKED).count();
            
            roomData.put("labels", List.of("Available", "Occupied", "Maintenance", "Out of Order"));
            roomData.put("data", List.of(availableRooms, occupiedRooms, maintenanceRooms, outOfOrderRooms));
            roomData.put("colors", List.of("#38a169", "#3182ce", "#d69e2e", "#e53e3e"));
            
            // Calculate occupancy rate
            long totalActiveRooms = availableRooms + occupiedRooms;
            double occupancyRate = totalActiveRooms > 0 ? (double) occupiedRooms / totalActiveRooms * 100 : 0;
            roomData.put("occupancyRate", Math.round(occupancyRate * 100.0) / 100.0);
            
            // Add total room count
            roomData.put("totalRooms", allRooms.size());
            
            return ResponseEntity.ok(roomData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Get event spaces analytics
    @GetMapping("/analytics/eventspaces")
    public ResponseEntity<Map<String, Object>> getEventSpacesAnalytics() {
        try {
            Map<String, Object> eventSpacesData = new HashMap<>();
            
            List<EventSpace> allEventSpaces = eventSpaceRepository.findAll();
            long totalEventSpaces = allEventSpaces.stream().filter(EventSpace::isActive).count();
            
            eventSpacesData.put("totalEventSpaces", totalEventSpaces);
            
            return ResponseEntity.ok(eventSpacesData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Get user analytics
    @GetMapping("/analytics/users")
    public ResponseEntity<Map<String, Object>> getUserAnalytics() {
        try {
            Map<String, Object> userData = new HashMap<>();
            
            List<User> allUsers = userRepository.findAll();
            
            // Count users by role (no STAFF enum; use FRONT_DESK and PAYMENT_OFFICER)
            long guests = allUsers.stream().filter(user -> user.getRole() == User.UserRole.GUEST).count();
            long frontDesk = allUsers.stream().filter(user -> user.getRole() == User.UserRole.FRONT_DESK).count();
            long paymentOfficers = allUsers.stream().filter(user -> user.getRole() == User.UserRole.PAYMENT_OFFICER).count();
            long managers = allUsers.stream().filter(user -> user.getRole() == User.UserRole.MANAGER).count();
            long admins = allUsers.stream().filter(user -> user.getRole() == User.UserRole.ADMIN).count();
            
            userData.put("labels", List.of("Guests", "Front Desk", "Payment Officers", "Managers", "Admins"));
            userData.put("data", List.of(guests, frontDesk, paymentOfficers, managers, admins));
            userData.put("colors", List.of("#e53e3e", "#3182ce", "#38a169", "#d69e2e", "#3182ce"));
            userData.put("totalUsers", allUsers.size());
            
            return ResponseEntity.ok(userData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Get booking analytics
    @GetMapping("/analytics/bookings")
    public ResponseEntity<Map<String, Object>> getBookingAnalytics() {
        try {
            Map<String, Object> bookingData = new HashMap<>();
            
            List<Booking> allBookings = bookingRepository.findAll();
            
            // Count bookings by status
            long confirmed = allBookings.stream().filter(booking -> booking.getStatus() == Booking.BookingStatus.CONFIRMED).count();
            long pending = allBookings.stream().filter(booking -> booking.getStatus() == Booking.BookingStatus.PENDING).count();
            long checkedIn = allBookings.stream().filter(booking -> booking.getStatus() == Booking.BookingStatus.CHECKED_IN).count();
            long checkedOut = allBookings.stream().filter(booking -> booking.getStatus() == Booking.BookingStatus.CHECKED_OUT).count();
            long cancelled = allBookings.stream().filter(booking -> booking.getStatus() == Booking.BookingStatus.CANCELLED).count();
            
            bookingData.put("labels", List.of("Confirmed", "Pending", "Checked In", "Checked Out", "Cancelled"));
            bookingData.put("data", List.of(confirmed, pending, checkedIn, checkedOut, cancelled));
            bookingData.put("colors", List.of("#38a169", "#d69e2e", "#3b82f6", "#718096", "#e53e3e"));
            bookingData.put("totalBookings", allBookings.size());
            bookingData.put("currentGuests", checkedIn); // Add current guests count
            
            return ResponseEntity.ok(bookingData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Get comprehensive dashboard analytics
    @GetMapping("/analytics/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardAnalytics() {
        try {
            Map<String, Object> dashboardData = new HashMap<>();
            
            // Get all analytics in one call (default to month view)
            ResponseEntity<Map<String, Object>> revenueResponse =       getRevenueAnalytics("month");
            ResponseEntity<Map<String, Object>> roomResponse = getRoomAnalytics();
            ResponseEntity<Map<String, Object>> userResponse = getUserAnalytics();
            ResponseEntity<Map<String, Object>> bookingResponse = getBookingAnalytics();
            ResponseEntity<Map<String, Object>> eventSpacesResponse = getEventSpacesAnalytics();
            
            if (revenueResponse.getStatusCode().is2xxSuccessful()) {
                dashboardData.put("revenue", revenueResponse.getBody());
            }
            if (roomResponse.getStatusCode().is2xxSuccessful()) {
                dashboardData.put("rooms", roomResponse.getBody());
            }
            if (userResponse.getStatusCode().is2xxSuccessful()) {
                dashboardData.put("users", userResponse.getBody());
            }
            if (bookingResponse.getStatusCode().is2xxSuccessful()) {
                dashboardData.put("bookings", bookingResponse.getBody());
            }
            if (eventSpacesResponse.getStatusCode().is2xxSuccessful()) {
                dashboardData.put("eventSpaces", eventSpacesResponse.getBody());
            }
            
            return ResponseEntity.ok(dashboardData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Helper method to calculate monthly revenue from payments
    private BigDecimal calculateMonthlyRevenue() {
        LocalDate startOfMonth = LocalDate.now().withDayOfMonth(1);
        LocalDateTime startOfMonthDateTime = startOfMonth.atStartOfDay();
        
        return paymentRepository.findAll().stream()
            .filter(payment -> payment.getPaymentStatus() == com.sliit.goldenpalmresort.model.Payment.PaymentStatus.COMPLETED)
            .filter(payment -> payment.getPaymentDate() != null && payment.getPaymentDate().isAfter(startOfMonthDateTime))
            .map(payment -> payment.getAmount())
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
} 