package com.sliit.goldenpalmresort.service;

import com.sliit.goldenpalmresort.model.Booking;
import com.sliit.goldenpalmresort.model.EventBooking;
import com.sliit.goldenpalmresort.model.EventSpace;
import com.sliit.goldenpalmresort.model.Room;
import com.sliit.goldenpalmresort.model.User;
import com.sliit.goldenpalmresort.model.Payment;
import com.sliit.goldenpalmresort.repository.BookingRepository;
import com.sliit.goldenpalmresort.repository.EventBookingRepository;
import com.sliit.goldenpalmresort.repository.EventSpaceRepository;
import com.sliit.goldenpalmresort.repository.PaymentRepository;
import com.sliit.goldenpalmresort.repository.RoomRepository;
import com.sliit.goldenpalmresort.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializationService implements CommandLineRunner {
    
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
    
    @Override
    public void run(String... args) throws Exception {
        initializeUsers();
        initializeRooms();
        initializeEventSpaces();
        initializeSampleBookings();
        initializeSamplePayments();
        System.out.println("Data initialization completed successfully");
    }
    
    private void initializeUsers() {
        if (userRepository.count() == 0) {
            // Create admin user
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@goldenpalmresort.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setFirstName("Chaminda");
            admin.setLastName("Perera");
            admin.setPhone("+94 11 234 5678");
            admin.setRole(User.UserRole.ADMIN);
            userRepository.save(admin);
            
            // Create sample guest user
            User guest = new User();
            guest.setUsername("guest");
            guest.setEmail("guest@example.com");
            guest.setPassword(passwordEncoder.encode("guest123"));
            guest.setFirstName("John");
            guest.setLastName("Doe");
            guest.setPhone("+94 77 123 4567");
            guest.setRole(User.UserRole.GUEST);
            userRepository.save(guest);
            
            // Create manager user
            User manager = new User();
            manager.setUsername("manager");
            manager.setEmail("manager@goldenpalmresort.com");
            manager.setPassword(passwordEncoder.encode("manager123"));
            manager.setFirstName("Nadeesha");
            manager.setLastName("Jayawardena");
            manager.setPhone("+94 11 234 5679");
            manager.setRole(User.UserRole.MANAGER);
            userRepository.save(manager);
            
            // Create front desk user
            User frontDesk = new User();
            frontDesk.setUsername("frontdesk");
            frontDesk.setEmail("frontdesk@goldenpalmresort.com");
            frontDesk.setPassword(passwordEncoder.encode("frontdesk123"));
            frontDesk.setFirstName("Tharushi");
            frontDesk.setLastName("Senanayake");
            frontDesk.setPhone("+94 11 234 5680");
            frontDesk.setRole(User.UserRole.FRONT_DESK);
            userRepository.save(frontDesk);
            
            // Create payment officer user
            User paymentOfficer = new User();
            paymentOfficer.setUsername("payment");
            paymentOfficer.setEmail("payment@goldenpalmresort.com");
            paymentOfficer.setPassword(passwordEncoder.encode("payment123"));
            paymentOfficer.setFirstName("Dilan");
            paymentOfficer.setLastName("Abeykoon");
            paymentOfficer.setPhone("+94 11 234 5681");
            paymentOfficer.setRole(User.UserRole.PAYMENT_OFFICER);
            userRepository.save(paymentOfficer);
            
            System.out.println("Sample users created");
        }
    }
    
    private void initializeRooms() {
        if (roomRepository.count() == 0) {
            // Create standard rooms
            for (int i = 101; i <= 110; i++) {
                Room room = new Room();
                room.setRoomNumber(String.valueOf(i));
                room.setRoomType("Standard Room");
                room.setFloorNumber(1);
                room.setBasePrice(new BigDecimal("15000.00"));
                room.setCapacity(2);
                room.setDescription("Comfortable and cozy rooms perfect for business or leisure travelers.");
                room.setAmenities("Queen-size bed, Private bathroom, Free Wi-Fi, Daily housekeeping");
                room.setStatus(Room.RoomStatus.AVAILABLE);
                room.setActive(true);
                roomRepository.save(room);
            }
            
            // Create deluxe rooms
            for (int i = 201; i <= 205; i++) {
                Room room = new Room();
                room.setRoomNumber(String.valueOf(i));
                room.setRoomType("Deluxe Room");
                room.setFloorNumber(2);
                room.setBasePrice(new BigDecimal("25000.00"));
                room.setCapacity(3);
                room.setDescription("Spacious rooms with premium amenities and beautiful views.");
                room.setAmenities("King-size bed, Ocean view, Balcony, Mini bar");
                room.setStatus(Room.RoomStatus.AVAILABLE);
                room.setActive(true);
                roomRepository.save(room);
            }
            
            // Create suites
            for (int i = 301; i <= 303; i++) {
                Room room = new Room();
                room.setRoomNumber(String.valueOf(i));
                room.setRoomType("Executive Suite");
                room.setFloorNumber(3);
                room.setBasePrice(new BigDecimal("45000.00"));
                room.setCapacity(4);
                room.setDescription("Ultimate luxury with separate living area and premium services.");
                room.setAmenities("Separate living room, Butler service, Private terrace, Premium amenities");
                room.setStatus(Room.RoomStatus.AVAILABLE);
                room.setActive(true);
                roomRepository.save(room);
            }
            
            System.out.println("Rooms initialized");
        }
    }
    
    private void initializeEventSpaces() {
        if (eventSpaceRepository.count() == 0) {
            // Grand Ballroom - Perfect for large weddings and corporate events
            EventSpace grandBallroom = new EventSpace();
            grandBallroom.setName("Grand Ballroom");
            grandBallroom.setDescription("Our largest and most elegant venue, perfect for grand weddings, corporate galas, and large-scale events. Features crystal chandeliers, marble floors, and panoramic windows.");
            grandBallroom.setCapacity(500);
            grandBallroom.setBasePrice(new BigDecimal("150000.00"));
            grandBallroom.setSetupTypes("Wedding,Corporate Event,Banquet,Conference");
            grandBallroom.setAmenities("Crystal chandeliers, Marble floors, Panoramic windows, Stage, Dance floor, Bridal suite, VIP lounge");
            grandBallroom.setFloorNumber(1);
            grandBallroom.setDimensions("30m x 20m");
            grandBallroom.setCateringAvailable(true);
            grandBallroom.setAudioVisualEquipment(true);
            grandBallroom.setParkingAvailable(true);
            grandBallroom.setStatus(EventSpace.EventSpaceStatus.AVAILABLE);
            grandBallroom.setActive(true);
            eventSpaceRepository.save(grandBallroom);
            
            // Royal Garden - Outdoor wedding venue
            EventSpace royalGarden = new EventSpace();
            royalGarden.setName("Royal Garden");
            royalGarden.setDescription("A stunning outdoor venue surrounded by tropical gardens, perfect for romantic weddings and outdoor celebrations. Features a gazebo, fountain, and lush landscaping.");
            royalGarden.setCapacity(200);
            royalGarden.setBasePrice(new BigDecimal("80000.00"));
            royalGarden.setSetupTypes("Wedding,Outdoor Event,Garden Party");
            royalGarden.setAmenities("Gazebo, Fountain, Tropical gardens, Outdoor lighting, Bridal suite, Restroom facilities");
            royalGarden.setFloorNumber(0);
            royalGarden.setDimensions("25m x 15m");
            royalGarden.setCateringAvailable(true);
            royalGarden.setAudioVisualEquipment(false);
            royalGarden.setParkingAvailable(true);
            royalGarden.setStatus(EventSpace.EventSpaceStatus.AVAILABLE);
            royalGarden.setActive(true);
            eventSpaceRepository.save(royalGarden);
            
            // Executive Conference Center - Business meetings and conferences
            EventSpace executiveConference = new EventSpace();
            executiveConference.setName("Executive Conference Center");
            executiveConference.setDescription("A state-of-the-art conference facility designed for business meetings, seminars, and corporate events. Features modern technology and professional setup.");
            executiveConference.setCapacity(150);
            executiveConference.setBasePrice(new BigDecimal("60000.00"));
            executiveConference.setSetupTypes("Conference,Meeting,Seminar,Training");
            executiveConference.setAmenities("Projector, Sound system, Microphones, Whiteboards, Coffee service, Business center");
            executiveConference.setFloorNumber(2);
            executiveConference.setDimensions("20m x 12m");
            executiveConference.setCateringAvailable(true);
            executiveConference.setAudioVisualEquipment(true);
            executiveConference.setParkingAvailable(true);
            executiveConference.setStatus(EventSpace.EventSpaceStatus.AVAILABLE);
            executiveConference.setActive(true);
            eventSpaceRepository.save(executiveConference);
            
            // Sunset Terrace - Intimate events
            EventSpace sunsetTerrace = new EventSpace();
            sunsetTerrace.setName("Sunset Terrace");
            sunsetTerrace.setDescription("An intimate rooftop venue with breathtaking ocean views, perfect for small weddings, cocktail parties, and intimate gatherings.");
            sunsetTerrace.setCapacity(80);
            sunsetTerrace.setBasePrice(new BigDecimal("45000.00"));
            sunsetTerrace.setSetupTypes("Wedding,Cocktail Party,Intimate Event");
            sunsetTerrace.setAmenities("Ocean view, Rooftop setting, Bar area, Lounge seating, Sunset views");
            sunsetTerrace.setFloorNumber(5);
            sunsetTerrace.setDimensions("15m x 10m");
            sunsetTerrace.setCateringAvailable(true);
            sunsetTerrace.setAudioVisualEquipment(false);
            sunsetTerrace.setParkingAvailable(true);
            sunsetTerrace.setStatus(EventSpace.EventSpaceStatus.AVAILABLE);
            sunsetTerrace.setActive(true);
            eventSpaceRepository.save(sunsetTerrace);
            
            // Marina Hall - Medium-sized events
            EventSpace marinaHall = new EventSpace();
            marinaHall.setName("Marina Hall");
            marinaHall.setDescription("A versatile venue overlooking the marina, suitable for medium-sized events, product launches, and social gatherings.");
            marinaHall.setCapacity(120);
            marinaHall.setBasePrice(new BigDecimal("70000.00"));
            marinaHall.setSetupTypes("Corporate Event,Product Launch,Social Event,Wedding");
            marinaHall.setAmenities("Marina view, Flexible layout, Stage, Dance floor, Bar area");
            marinaHall.setFloorNumber(1);
            marinaHall.setDimensions("18m x 14m");
            marinaHall.setCateringAvailable(true);
            marinaHall.setAudioVisualEquipment(true);
            marinaHall.setParkingAvailable(true);
            marinaHall.setStatus(EventSpace.EventSpaceStatus.AVAILABLE);
            marinaHall.setActive(true);
            eventSpaceRepository.save(marinaHall);
            
            System.out.println("Event spaces initialized");
        }
    }
    
    private void initializeSampleBookings() {
        if (bookingRepository.count() == 0 && eventBookingRepository.count() == 0) {
            // Get sample users
            User guest = userRepository.findByUsername("guest").orElse(null);
            User admin = userRepository.findByUsername("admin").orElse(null);
            
            // Get sample rooms
            Room room101 = roomRepository.findByRoomNumber("101");
            Room room205 = roomRepository.findByRoomNumber("205");
            
            // Get sample event spaces
            EventSpace grandBallroom = eventSpaceRepository.findByName("Grand Ballroom");
            EventSpace royalGarden = eventSpaceRepository.findByName("Royal Garden");
            
            if (guest != null && room101 != null) {
                // Create sample room booking
                Booking roomBooking = new Booking();
                roomBooking.setBookingReference("BK001");
                roomBooking.setUser(guest);
                roomBooking.setRoom(room101);
                roomBooking.setCheckInDate(java.time.LocalDate.now().plusDays(1));
                roomBooking.setCheckOutDate(java.time.LocalDate.now().plusDays(3));
                roomBooking.setGuestCount(2);
                roomBooking.setTotalAmount(new BigDecimal("450.00"));
                roomBooking.setSpecialRequests("Early check-in preferred");
                roomBooking.setStatus(Booking.BookingStatus.PENDING);
                roomBooking.setCreatedAt(java.time.LocalDateTime.now());
                roomBooking.setUpdatedAt(java.time.LocalDateTime.now());
                bookingRepository.save(roomBooking);
                
                // Create confirmed booking
                Booking confirmedBooking = new Booking();
                confirmedBooking.setBookingReference("BK002");
                confirmedBooking.setUser(guest);
                confirmedBooking.setRoom(room205);
                confirmedBooking.setCheckInDate(java.time.LocalDate.now().plusDays(5));
                confirmedBooking.setCheckOutDate(java.time.LocalDate.now().plusDays(7));
                confirmedBooking.setGuestCount(1);
                confirmedBooking.setTotalAmount(new BigDecimal("300.00"));
                confirmedBooking.setSpecialRequests("Late check-out if possible");
                confirmedBooking.setStatus(Booking.BookingStatus.CONFIRMED);
                confirmedBooking.setCreatedAt(java.time.LocalDateTime.now().minusDays(2));
                confirmedBooking.setUpdatedAt(java.time.LocalDateTime.now().minusDays(1));
                bookingRepository.save(confirmedBooking);
                
                // Create checked-in booking
                Booking checkedInBooking = new Booking();
                checkedInBooking.setBookingReference("BK003");
                checkedInBooking.setUser(guest);
                checkedInBooking.setRoom(room101);
                checkedInBooking.setCheckInDate(java.time.LocalDate.now());
                checkedInBooking.setCheckOutDate(java.time.LocalDate.now().plusDays(2));
                checkedInBooking.setGuestCount(2);
                checkedInBooking.setTotalAmount(new BigDecimal("400.00"));
                checkedInBooking.setSpecialRequests("High floor preferred");
                checkedInBooking.setStatus(Booking.BookingStatus.CHECKED_IN);
                checkedInBooking.setCreatedAt(java.time.LocalDateTime.now().minusDays(1));
                checkedInBooking.setUpdatedAt(java.time.LocalDateTime.now());
                bookingRepository.save(checkedInBooking);
                
                // Create checked-out booking
                Booking checkedOutBooking = new Booking();
                checkedOutBooking.setBookingReference("BK004");
                checkedOutBooking.setUser(guest);
                checkedOutBooking.setRoom(room205);
                checkedOutBooking.setCheckInDate(java.time.LocalDate.now().minusDays(3));
                checkedOutBooking.setCheckOutDate(java.time.LocalDate.now());
                checkedOutBooking.setGuestCount(1);
                checkedOutBooking.setTotalAmount(new BigDecimal("300.00"));
                checkedOutBooking.setSpecialRequests("Quiet room preferred");
                checkedOutBooking.setStatus(Booking.BookingStatus.CHECKED_OUT);
                checkedOutBooking.setCreatedAt(java.time.LocalDateTime.now().minusDays(5));
                checkedOutBooking.setUpdatedAt(java.time.LocalDateTime.now());
                bookingRepository.save(checkedOutBooking);
            }
            
            if (guest != null && grandBallroom != null) {
                // Create sample event booking
                EventBooking eventBooking = new EventBooking();
                eventBooking.setBookingReference("EB001");
                eventBooking.setUser(guest);
                eventBooking.setEventSpace(grandBallroom);
                eventBooking.setEventType("Wedding");
                eventBooking.setEventDate(java.time.LocalDate.now().plusDays(10));
                eventBooking.setStartTime("18:00");
                eventBooking.setEndTime("22:00");
                eventBooking.setExpectedGuests(150);
                eventBooking.setTotalAmount(new BigDecimal("150000.00"));
                eventBooking.setSpecialRequests("Wedding ceremony setup required");
                eventBooking.setStatus(EventBooking.EventBookingStatus.PENDING);
                eventBooking.setCreatedAt(java.time.LocalDateTime.now());
                eventBooking.setUpdatedAt(java.time.LocalDateTime.now());
                eventBookingRepository.save(eventBooking);
                
                // Create confirmed event booking
                EventBooking confirmedEventBooking = new EventBooking();
                confirmedEventBooking.setBookingReference("EB002");
                confirmedEventBooking.setUser(guest);
                confirmedEventBooking.setEventSpace(royalGarden);
                confirmedEventBooking.setEventType("Wedding");
                confirmedEventBooking.setEventDate(java.time.LocalDate.now().plusDays(15));
                confirmedEventBooking.setStartTime("16:00");
                confirmedEventBooking.setEndTime("20:00");
                confirmedEventBooking.setExpectedGuests(80);
                confirmedEventBooking.setTotalAmount(new BigDecimal("80000.00"));
                confirmedEventBooking.setSpecialRequests("Garden wedding setup");
                confirmedEventBooking.setStatus(EventBooking.EventBookingStatus.CONFIRMED);
                confirmedEventBooking.setCreatedAt(java.time.LocalDateTime.now().minusDays(5));
                confirmedEventBooking.setUpdatedAt(java.time.LocalDateTime.now().minusDays(3));
                eventBookingRepository.save(confirmedEventBooking);
            }
            
            System.out.println("Sample bookings initialized");
        }
    }
    
    private void initializeSamplePayments() {
        if (paymentRepository.count() == 0) {
            System.out.println("Initializing sample payments...");
            
            // Get existing bookings and users
            User guest = userRepository.findByUsername("guest").orElse(null);
            Booking roomBooking = bookingRepository.findByBookingReference("BK001").orElse(null);
            Booking confirmedBooking = bookingRepository.findByBookingReference("BK002").orElse(null);
            EventBooking eventBooking = eventBookingRepository.findByBookingReference("EB001").orElse(null);
            EventBooking confirmedEventBooking = eventBookingRepository.findByBookingReference("EB002").orElse(null);
            
            if (guest != null && roomBooking != null) {
                // Create completed payment for room booking
                Payment completedPayment = new Payment();
                completedPayment.setBooking(roomBooking);
                completedPayment.setAmount(new BigDecimal("450.00"));
                completedPayment.setPaymentMethod(Payment.PaymentMethod.CREDIT_CARD);
                completedPayment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);
                completedPayment.setTransactionId("TXN001");
                completedPayment.setPaymentDate(java.time.LocalDateTime.now().minusDays(1));
                completedPayment.setProcessedBy("payment");
                completedPayment.setNotes("Payment processed successfully");
                paymentRepository.save(completedPayment);
                
                // Create pending payment for confirmed booking
                Payment pendingPayment = new Payment();
                pendingPayment.setBooking(confirmedBooking);
                pendingPayment.setAmount(new BigDecimal("300.00"));
                pendingPayment.setPaymentMethod(Payment.PaymentMethod.BANK_TRANSFER);
                pendingPayment.setPaymentStatus(Payment.PaymentStatus.PENDING);
                pendingPayment.setTransactionId("TXN002");
                pendingPayment.setProcessedBy("payment");
                pendingPayment.setNotes("Awaiting bank confirmation");
                paymentRepository.save(pendingPayment);
                
                // Create failed payment
                Payment failedPayment = new Payment();
                failedPayment.setBooking(roomBooking);
                failedPayment.setAmount(new BigDecimal("450.00"));
                failedPayment.setPaymentMethod(Payment.PaymentMethod.DEBIT_CARD);
                failedPayment.setPaymentStatus(Payment.PaymentStatus.FAILED);
                failedPayment.setTransactionId("TXN003");
                failedPayment.setProcessedBy("payment");
                failedPayment.setNotes("Card declined - insufficient funds");
                paymentRepository.save(failedPayment);
                
                // Create refunded payment
                Payment refundedPayment = new Payment();
                refundedPayment.setBooking(confirmedBooking);
                refundedPayment.setAmount(new BigDecimal("300.00"));
                refundedPayment.setPaymentMethod(Payment.PaymentMethod.CASH);
                refundedPayment.setPaymentStatus(Payment.PaymentStatus.REFUNDED);
                refundedPayment.setTransactionId("TXN004");
                refundedPayment.setPaymentDate(java.time.LocalDateTime.now().minusDays(3));
                refundedPayment.setRefundAmount(new BigDecimal("300.00"));
                refundedPayment.setRefundReason("Guest cancellation");
                refundedPayment.setRefundDate(java.time.LocalDateTime.now().minusDays(1));
                refundedPayment.setProcessedBy("payment");
                refundedPayment.setNotes("Full refund processed");
                paymentRepository.save(refundedPayment);
            }
            
            if (guest != null && eventBooking != null) {
                // Create completed payment for event booking
                Payment eventPayment = new Payment();
                eventPayment.setEventBooking(eventBooking);
                eventPayment.setAmount(new BigDecimal("150000.00"));
                eventPayment.setPaymentMethod(Payment.PaymentMethod.QR_PAYMENT);
                eventPayment.setPaymentStatus(Payment.PaymentStatus.COMPLETED);
                eventPayment.setTransactionId("TXN005");
                eventPayment.setPaymentDate(java.time.LocalDateTime.now().minusDays(2));
                eventPayment.setProcessedBy("payment");
                eventPayment.setNotes("QR payment successful");
                paymentRepository.save(eventPayment);
                
                // Create pending payment for confirmed event booking
                Payment pendingEventPayment = new Payment();
                pendingEventPayment.setEventBooking(confirmedEventBooking);
                pendingEventPayment.setAmount(new BigDecimal("80000.00"));
                pendingEventPayment.setPaymentMethod(Payment.PaymentMethod.BANK_TRANSFER);
                pendingEventPayment.setPaymentStatus(Payment.PaymentStatus.PENDING);
                pendingEventPayment.setTransactionId("TXN006");
                pendingEventPayment.setProcessedBy("payment");
                pendingEventPayment.setNotes("Bank transfer in progress");
                paymentRepository.save(pendingEventPayment);
            }
            
            System.out.println("Sample payments initialized");
        }
    }
} 