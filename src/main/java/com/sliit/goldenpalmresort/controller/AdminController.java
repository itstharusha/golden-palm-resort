package com.sliit.goldenpalmresort.controller;

import com.sliit.goldenpalmresort.dto.RegisterRequest;
import com.sliit.goldenpalmresort.dto.RoomUpdateRequest;
import com.sliit.goldenpalmresort.dto.EventSpaceUpdateRequest;
import com.sliit.goldenpalmresort.model.Booking;
import com.sliit.goldenpalmresort.model.EventSpace;
import com.sliit.goldenpalmresort.model.Room;
import com.sliit.goldenpalmresort.model.User;
import com.sliit.goldenpalmresort.repository.BookingRepository;
import com.sliit.goldenpalmresort.repository.EventSpaceRepository;
import com.sliit.goldenpalmresort.repository.RoomRepository;
import com.sliit.goldenpalmresort.repository.UserRepository;
import com.sliit.goldenpalmresort.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
    private BookingRepository bookingRepository;

    @Autowired
    private AuthService authService;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
            
            // Monthly revenue (placeholder calculation)
            BigDecimal monthlyRevenue = BigDecimal.valueOf(15420.00);
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
            List<Booking> recentBookings = bookingRepository.findAll().stream()
                    .limit(5)
                    .toList();

            List<Map<String, Object>> bookingsData = recentBookings.stream()
                    .map(booking -> {
                        Map<String, Object> bookingData = new HashMap<>();
                        bookingData.put("bookingReference", booking.getBookingReference());
                        bookingData.put("guestName", booking.getUser().getFirstName() + " " + booking.getUser().getLastName());
                        bookingData.put("type", "Room");
                        bookingData.put("checkInDate", booking.getCheckInDate().toString());
                        bookingData.put("checkOutDate", booking.getCheckOutDate().toString());
                        bookingData.put("status", booking.getStatus().name());
                        bookingData.put("totalAmount", booking.getTotalAmount());
                        return bookingData;
                    })
                    .toList();

            return ResponseEntity.ok(bookingsData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // Get all bookings
    @GetMapping("/bookings")
    public ResponseEntity<List<Map<String, Object>>> getAllBookings() {
        try {
            List<Booking> allBookings = bookingRepository.findAll();

            List<Map<String, Object>> bookingsData = allBookings.stream()
                    .map(booking -> {
                        Map<String, Object> bookingData = new HashMap<>();
                        bookingData.put("bookingReference", booking.getBookingReference());
                        bookingData.put("guestName", booking.getUser().getFirstName() + " " + booking.getUser().getLastName());
                        bookingData.put("type", "Room");
                        bookingData.put("checkInDate", booking.getCheckInDate().toString());
                        bookingData.put("checkOutDate", booking.getCheckOutDate().toString());
                        bookingData.put("status", booking.getStatus().name());
                        bookingData.put("totalAmount", booking.getTotalAmount());
                        return bookingData;
                    })
                    .toList();

            return ResponseEntity.ok(bookingsData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
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
} 