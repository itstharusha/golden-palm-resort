package com.sliit.goldenpalmresort.controller;

import com.sliit.goldenpalmresort.model.*;
import com.sliit.goldenpalmresort.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/manager")
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('MANAGER')")
public class ManagerController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private EventSpaceRepository eventSpaceRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EventBookingRepository eventBookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==================== MANAGER DASHBOARD ====================
    
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getManagerDashboard() {
        try {
            Map<String, Object> dashboard = new HashMap<>();
            
            // Operational Statistics
            dashboard.put("totalRooms", roomRepository.count());
            dashboard.put("availableRooms", roomRepository.findByStatus(Room.RoomStatus.AVAILABLE).size());
            dashboard.put("occupiedRooms", roomRepository.findByStatus(Room.RoomStatus.OCCUPIED).size());
            dashboard.put("maintenanceRooms", roomRepository.findByStatus(Room.RoomStatus.MAINTENANCE).size());
            
            // Booking Statistics
            dashboard.put("todayCheckIns", getTodayCheckIns());
            dashboard.put("todayCheckOuts", getTodayCheckOuts());
            dashboard.put("pendingBookings", bookingRepository.findByStatus(Booking.BookingStatus.PENDING).size());
            dashboard.put("confirmedBookings", bookingRepository.findByStatus(Booking.BookingStatus.CONFIRMED).size());
            
            // Revenue Analytics
            dashboard.put("todayRevenue", getTodayRevenue());
            dashboard.put("monthlyRevenue", getMonthlyRevenue());
            dashboard.put("occupancyRate", calculateOccupancyRate());
            
            // Staff Overview
            dashboard.put("frontDeskStaff", userRepository.findByRole(User.UserRole.FRONT_DESK).size());
            dashboard.put("paymentOfficers", userRepository.findByRole(User.UserRole.PAYMENT_OFFICER).size());
            
            return ResponseEntity.ok(dashboard);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== STAFF MANAGEMENT (Limited to non-admin roles) ====================
    
    @GetMapping("/staff")
    public ResponseEntity<List<Map<String, Object>>> getStaff() {
        try {
            List<User> staff = userRepository.findAll().stream()
                .filter(user -> user.getRole() == User.UserRole.FRONT_DESK || 
                               user.getRole() == User.UserRole.PAYMENT_OFFICER ||
                               user.getRole() == User.UserRole.GUEST)
                .collect(Collectors.toList());
            
            List<Map<String, Object>> staffData = staff.stream()
                .map(this::mapUserToResponse)
                .collect(Collectors.toList());
            
            return ResponseEntity.ok(staffData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/staff")
    public ResponseEntity<?> createStaffMember(@RequestBody Map<String, Object> staffData) {
        try {
            String role = (String) staffData.get("role");
            
            // Managers can only create FRONT_DESK, PAYMENT_OFFICER, or GUEST users
            if (!role.equals("FRONT_DESK") && !role.equals("PAYMENT_OFFICER") && !role.equals("GUEST")) {
                return ResponseEntity.badRequest().body(Map.of("message", "Managers can only create Front Desk, Payment Officer, or Guest accounts"));
            }
            
            String username = (String) staffData.get("username");
            String email = (String) staffData.get("email");
            
            if (userRepository.findByUsername(username).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Username already exists"));
            }
            if (userRepository.findByEmail(email).isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Email already exists"));
            }

            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode((String) staffData.get("password")));
            user.setFirstName((String) staffData.get("firstName"));
            user.setLastName((String) staffData.get("lastName"));
            user.setPhone((String) staffData.get("phone"));
            user.setRole(User.UserRole.valueOf(role));
            user.setActive(true);

            user = userRepository.save(user);
            return ResponseEntity.ok(mapUserToResponse(user));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error creating staff member: " + e.getMessage()));
        }
    }

    @PutMapping("/staff/{userId}")
    public ResponseEntity<?> updateStaffMember(@PathVariable Long userId, @RequestBody Map<String, Object> staffData) {
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            User user = userOpt.get();
            
            // Prevent managers from editing admin or manager accounts
            if (user.getRole() == User.UserRole.ADMIN || user.getRole() == User.UserRole.MANAGER) {
                return ResponseEntity.badRequest().body(Map.of("message", "Cannot modify admin or manager accounts"));
            }

            if (staffData.containsKey("firstName")) user.setFirstName((String) staffData.get("firstName"));
            if (staffData.containsKey("lastName")) user.setLastName((String) staffData.get("lastName"));
            if (staffData.containsKey("email")) user.setEmail((String) staffData.get("email"));
            if (staffData.containsKey("phone")) user.setPhone((String) staffData.get("phone"));
            if (staffData.containsKey("isActive")) user.setActive((Boolean) staffData.get("isActive"));
            
            // Role changes limited to non-admin roles
            if (staffData.containsKey("role")) {
                String newRole = (String) staffData.get("role");
                if (newRole.equals("FRONT_DESK") || newRole.equals("PAYMENT_OFFICER") || newRole.equals("GUEST")) {
                    user.setRole(User.UserRole.valueOf(newRole));
                }
            }

            user = userRepository.save(user);
            return ResponseEntity.ok(mapUserToResponse(user));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error updating staff member: " + e.getMessage()));
        }
    }

    // ==================== BOOKING MANAGEMENT ====================
    
    @GetMapping("/bookings")
    public ResponseEntity<List<Map<String, Object>>> getAllBookings() {
        try {
            List<Booking> bookings = bookingRepository.findAll();
            List<Map<String, Object>> bookingData = bookings.stream()
                .map(this::mapBookingToResponse)
                .collect(Collectors.toList());
            return ResponseEntity.ok(bookingData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/bookings/pending")
    public ResponseEntity<List<Map<String, Object>>> getPendingBookings() {
        try {
            List<Booking> pendingBookings = bookingRepository.findByStatus(Booking.BookingStatus.PENDING);
            List<Map<String, Object>> bookingData = pendingBookings.stream()
                .map(this::mapBookingToResponse)
                .collect(Collectors.toList());
            return ResponseEntity.ok(bookingData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/bookings/{bookingId}/approve")
    public ResponseEntity<?> approveBooking(@PathVariable Long bookingId) {
        try {
            Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
            if (bookingOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Booking booking = bookingOpt.get();
            booking.setStatus(Booking.BookingStatus.CONFIRMED);
            
            // Update room status to occupied if check-in date is today or past
            if (!booking.getCheckInDate().isAfter(LocalDate.now())) {
                Room room = booking.getRoom();
                room.setStatus(Room.RoomStatus.OCCUPIED);
                roomRepository.save(room);
            }
            
            booking = bookingRepository.save(booking);
            return ResponseEntity.ok(mapBookingToResponse(booking));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error approving booking: " + e.getMessage()));
        }
    }

    @PutMapping("/bookings/{bookingId}/cancel")
    public ResponseEntity<?> cancelBooking(@PathVariable Long bookingId, @RequestBody Map<String, String> cancelData) {
        try {
            Optional<Booking> bookingOpt = bookingRepository.findById(bookingId);
            if (bookingOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Booking booking = bookingOpt.get();
            booking.setStatus(Booking.BookingStatus.CANCELLED);
            
            // Free up the room
            Room room = booking.getRoom();
            room.setStatus(Room.RoomStatus.AVAILABLE);
            roomRepository.save(room);
            
            booking = bookingRepository.save(booking);
            return ResponseEntity.ok(mapBookingToResponse(booking));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error cancelling booking: " + e.getMessage()));
        }
    }

    // ==================== ROOM OPERATIONS (Status changes only) ====================
    
    @GetMapping("/rooms")
    public ResponseEntity<List<Room>> getAllRooms() {
        try {
            List<Room> rooms = roomRepository.findAll();
            return ResponseEntity.ok(rooms);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @PutMapping("/rooms/{roomId}/status")
    public ResponseEntity<?> updateRoomStatus(@PathVariable Long roomId, @RequestBody Map<String, String> statusData) {
        try {
            Optional<Room> roomOpt = roomRepository.findById(roomId);
            if (roomOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }

            Room room = roomOpt.get();
            String newStatus = statusData.get("status");
            room.setStatus(Room.RoomStatus.valueOf(newStatus));
            
            room = roomRepository.save(room);
            return ResponseEntity.ok(room);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("message", "Error updating room status: " + e.getMessage()));
        }
    }

    // ==================== ANALYTICS & REPORTS ====================
    
    @GetMapping("/analytics/revenue")
    public ResponseEntity<Map<String, Object>> getRevenueAnalytics() {
        try {
            Map<String, Object> analytics = new HashMap<>();
            
            analytics.put("todayRevenue", getTodayRevenue());
            analytics.put("weeklyRevenue", getWeeklyRevenue());
            analytics.put("monthlyRevenue", getMonthlyRevenue());
            analytics.put("yearlyRevenue", getYearlyRevenue());
            
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/analytics/occupancy")
    public ResponseEntity<Map<String, Object>> getOccupancyAnalytics() {
        try {
            Map<String, Object> analytics = new HashMap<>();
            
            analytics.put("currentOccupancyRate", calculateOccupancyRate());
            analytics.put("averageStayDuration", calculateAverageStayDuration());
            analytics.put("peakOccupancyDays", getPeakOccupancyDays());
            
            return ResponseEntity.ok(analytics);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ==================== HELPER METHODS ====================
    
    private Map<String, Object> mapUserToResponse(User user) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", user.getId());
        userData.put("username", user.getUsername());
        userData.put("email", user.getEmail());
        userData.put("firstName", user.getFirstName());
        userData.put("lastName", user.getLastName());
        userData.put("phone", user.getPhone());
        userData.put("role", user.getRole().name());
        userData.put("isActive", user.isActive());
        userData.put("createdAt", user.getCreatedAt());
        return userData;
    }

    private Map<String, Object> mapBookingToResponse(Booking booking) {
        Map<String, Object> bookingData = new HashMap<>();
        bookingData.put("id", booking.getId());
        bookingData.put("bookingReference", booking.getBookingReference());
        bookingData.put("guestName", booking.getUser().getFirstName() + " " + booking.getUser().getLastName());
        bookingData.put("roomNumber", booking.getRoom().getRoomNumber());
        bookingData.put("checkInDate", booking.getCheckInDate().toString());
        bookingData.put("checkOutDate", booking.getCheckOutDate().toString());
        bookingData.put("guestCount", booking.getGuestCount());
        bookingData.put("totalAmount", booking.getTotalAmount());
        bookingData.put("status", booking.getStatus().name());
        bookingData.put("specialRequests", booking.getSpecialRequests());
        return bookingData;
    }

    private long getTodayCheckIns() {
        return bookingRepository.findAll().stream()
            .filter(b -> b.getCheckInDate().equals(LocalDate.now()))
            .count();
    }

    private long getTodayCheckOuts() {
        return bookingRepository.findAll().stream()
            .filter(b -> b.getCheckOutDate().equals(LocalDate.now()))
            .count();
    }

    private BigDecimal getTodayRevenue() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().plusDays(1).atStartOfDay();
        
        return paymentRepository.findAll().stream()
            .filter(p -> p.getPaymentStatus() == Payment.PaymentStatus.COMPLETED)
            .filter(p -> p.getPaymentDate() != null && 
                        p.getPaymentDate().isAfter(startOfDay) && 
                        p.getPaymentDate().isBefore(endOfDay))
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getWeeklyRevenue() {
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        
        return paymentRepository.findAll().stream()
            .filter(p -> p.getPaymentStatus() == Payment.PaymentStatus.COMPLETED)
            .filter(p -> p.getPaymentDate() != null && p.getPaymentDate().isAfter(weekAgo))
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getMonthlyRevenue() {
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        
        return paymentRepository.findAll().stream()
            .filter(p -> p.getPaymentStatus() == Payment.PaymentStatus.COMPLETED)
            .filter(p -> p.getPaymentDate() != null && p.getPaymentDate().isAfter(startOfMonth))
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal getYearlyRevenue() {
        LocalDateTime startOfYear = LocalDate.now().withDayOfYear(1).atStartOfDay();
        
        return paymentRepository.findAll().stream()
            .filter(p -> p.getPaymentStatus() == Payment.PaymentStatus.COMPLETED)
            .filter(p -> p.getPaymentDate() != null && p.getPaymentDate().isAfter(startOfYear))
            .map(Payment::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int calculateOccupancyRate() {
        long totalRooms = roomRepository.count();
        long occupiedRooms = roomRepository.findByStatus(Room.RoomStatus.OCCUPIED).size();
        return totalRooms > 0 ? (int) Math.round((double) occupiedRooms / totalRooms * 100) : 0;
    }

    private double calculateAverageStayDuration() {
        List<Booking> completedBookings = bookingRepository.findByStatus(Booking.BookingStatus.CHECKED_OUT);
        if (completedBookings.isEmpty()) return 0;
        
        double totalDays = completedBookings.stream()
            .mapToLong(b -> b.getCheckInDate().until(b.getCheckOutDate()).getDays())
            .average()
            .orElse(0);
        
        return Math.round(totalDays * 100.0) / 100.0;
    }

    private List<String> getPeakOccupancyDays() {
        // Calculate peak occupancy days from actual booking data
        try {
            Map<String, Long> dayBookingCounts = bookingRepository.findAll().stream()
                .filter(booking -> booking.getStatus() == Booking.BookingStatus.CONFIRMED || 
                                 booking.getStatus() == Booking.BookingStatus.CHECKED_IN)
                .collect(Collectors.groupingBy(
                    booking -> booking.getCheckInDate().getDayOfWeek().name(),
                    Collectors.counting()
                ));
            
            // Return top 3 days by booking count
            return dayBookingCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(3)
                .map(Map.Entry::getKey)
                .map(day -> day.substring(0, 1) + day.substring(1).toLowerCase()) // Format: Friday
                .collect(Collectors.toList());
        } catch (Exception e) {
            // Fallback to common peak days if calculation fails
            return List.of("Friday", "Saturday", "Sunday");
        }
    }
}
