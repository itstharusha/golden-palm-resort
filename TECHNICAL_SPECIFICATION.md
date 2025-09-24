# Golden Palm Resort - Hotel Reservation System for Special Events
## Technical Specification Document

**Project:** SLIIT 2025-Y2-S1-MLB-B1G2-02  
**System:** Web-Based Hotel Reservation System for Special Events  
**Version:** 1.0  
**Date:** December 2024  

---

## 📋 Table of Contents
1. [Executive Summary](#executive-summary)
2. [System Overview](#system-overview)
3. [Architecture Design](#architecture-design)
4. [Database Design](#database-design)
5. [API Specifications](#api-specifications)
6. [Security Framework](#security-framework)
7. [User Interface Design](#user-interface-design)
8. [Business Logic Implementation](#business-logic-implementation)
9. [Integration Requirements](#integration-requirements)
10. [Testing Strategy](#testing-strategy)
11. [Deployment & DevOps](#deployment--devops)
12. [Performance & Scalability](#performance--scalability)
13. [Maintenance & Support](#maintenance--support)

---

## 🎯 Executive Summary

The Golden Palm Resort Hotel Reservation System is a comprehensive web-based solution designed to streamline hotel operations for special events. The system serves multiple stakeholders including guests, event organizers, hotel staff, and administrators, providing a seamless booking experience while ensuring operational efficiency.

### Key Features
- **Multi-role User Management** with role-based access control
- **Real-time Room & Event Space Booking** with availability tracking
- **Secure Payment Processing** with multiple payment methods
- **Automated Notifications** via email and SMS
- **Comprehensive Reporting** and analytics dashboard
- **Mobile-responsive Design** for all devices

### Technology Stack
- **Backend:** Java 17, Spring Boot 3.1.4, Spring Security, JPA/Hibernate
- **Frontend:** HTML5, CSS3, JavaScript, Bootstrap 5
- **Database:** MySQL 8.x
- **Build Tool:** Apache Maven
- **Deployment:** Apache Tomcat

---

## 🏗️ System Overview

### Stakeholder Analysis

| Role | User | Primary Functions | Access Level |
|------|------|------------------|--------------|
| **Hotel Manager** | Mr. Chaminda Perera | System-wide analytics, reporting, strategic oversight | Admin Dashboard |
| **Event Organizer** | Ms. Nadeesha Jayawardena | Event space booking, multi-room reservations | Event Booking Portal |
| **System Administrator** | Mr. Ruwan Fernando | System configuration, user management, pricing | Full Admin Access |
| **Front Desk Officer** | Ms. Tharushi Senanayake | Check-in/check-out, guest management | Front Desk Module |
| **Payment Officer** | Mr. Dilan Abeykoon | Payment verification, refunds, invoicing | Finance Module |
| **Guests** | External Users | Room booking, reservation management | Guest Portal |

### Core Modules

1. **Authentication & Authorization Module**
2. **Room & Event Space Management Module**
3. **Booking & Reservation Module**
4. **Payment Processing Module**
5. **Guest Management Module**
6. **Reporting & Analytics Module**
7. **Notification System Module**

---

## 🏛️ Architecture Design

### System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Presentation Layer                        │
├─────────────────────────────────────────────────────────────┤
│  Guest Portal │ Admin Dashboard │ Staff Portal │ Mobile UI   │
└─────────────────────────────────────────────────────────────┘
                                │
┌─────────────────────────────────────────────────────────────┐
│                    Application Layer                         │
├─────────────────────────────────────────────────────────────┤
│  Controllers │ Services │ Security │ Validation │ Exception   │
└─────────────────────────────────────────────────────────────┘
                                │
┌─────────────────────────────────────────────────────────────┐
│                    Business Logic Layer                      │
├─────────────────────────────────────────────────────────────┤
│  Booking Logic │ Payment Logic │ Notification Logic │ Reports │
└─────────────────────────────────────────────────────────────┘
                                │
┌─────────────────────────────────────────────────────────────┐
│                    Data Access Layer                         │
├─────────────────────────────────────────────────────────────┤
│  Repositories │ JPA/Hibernate │ Database Connection Pool     │
└─────────────────────────────────────────────────────────────┘
                                │
┌─────────────────────────────────────────────────────────────┐
│                    Data Storage Layer                        │
├─────────────────────────────────────────────────────────────┤
│  MySQL Database │ File Storage │ Cache (Redis - Optional)    │
└─────────────────────────────────────────────────────────────┘
```

### Package Structure

```
com.sliit.goldenpalmresort/
├── GoldenPalmResortApplication.java
├── config/
│   ├── SecurityConfig.java
│   ├── DatabaseConfig.java
│   └── WebConfig.java
├── controller/
│   ├── AuthController.java
│   ├── BookingController.java
│   ├── RoomController.java
│   ├── PaymentController.java
│   ├── AdminController.java
│   └── ReportController.java
├── service/
│   ├── AuthService.java
│   ├── BookingService.java
│   ├── RoomService.java
│   ├── PaymentService.java
│   ├── NotificationService.java
│   └── ReportService.java
├── repository/
│   ├── UserRepository.java
│   ├── RoomRepository.java
│   ├── BookingRepository.java
│   ├── PaymentRepository.java
│   └── EventRepository.java
├── model/
│   ├── User.java
│   ├── Room.java
│   ├── Booking.java
│   ├── Payment.java
│   ├── Event.java
│   └── Notification.java
├── dto/
│   ├── BookingRequest.java
│   ├── PaymentRequest.java
│   ├── UserRegistration.java
│   └── ReportResponse.java
├── exception/
│   ├── GlobalExceptionHandler.java
│   ├── BookingException.java
│   └── PaymentException.java
└── util/
    ├── JwtUtil.java
    ├── EmailUtil.java
    └── ValidationUtil.java
```

---

## 🗄️ Database Design

### Entity Relationship Diagram

```sql
-- Users and Authentication
CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    first_name VARCHAR(50) NOT NULL,
    last_name VARCHAR(50) NOT NULL,
    phone VARCHAR(20),
    role ENUM('GUEST', 'ADMIN', 'MANAGER', 'FRONT_DESK', 'PAYMENT_OFFICER') NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Room Types and Categories
CREATE TABLE room_types (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    base_price DECIMAL(10,2) NOT NULL,
    capacity INT NOT NULL,
    amenities JSON,
    is_active BOOLEAN DEFAULT TRUE
);

-- Individual Rooms
CREATE TABLE rooms (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    room_number VARCHAR(20) UNIQUE NOT NULL,
    room_type_id BIGINT NOT NULL,
    floor_number INT,
    status ENUM('AVAILABLE', 'OCCUPIED', 'MAINTENANCE', 'BLOCKED') DEFAULT 'AVAILABLE',
    FOREIGN KEY (room_type_id) REFERENCES room_types(id)
);

-- Event Spaces
CREATE TABLE event_spaces (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    capacity INT NOT NULL,
    base_price DECIMAL(10,2) NOT NULL,
    setup_types JSON, -- Wedding, Conference, Party setups
    is_active BOOLEAN DEFAULT TRUE
);

-- Bookings
CREATE TABLE bookings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_reference VARCHAR(20) UNIQUE NOT NULL,
    user_id BIGINT NOT NULL,
    room_id BIGINT,
    event_space_id BIGINT,
    check_in_date DATE NOT NULL,
    check_out_date DATE NOT NULL,
    guest_count INT NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    status ENUM('PENDING', 'CONFIRMED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED') DEFAULT 'PENDING',
    special_requests TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (room_id) REFERENCES rooms(id),
    FOREIGN KEY (event_space_id) REFERENCES event_spaces(id)
);

-- Payments
CREATE TABLE payments (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    booking_id BIGINT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_method ENUM('CREDIT_CARD', 'DEBIT_CARD', 'QR_PAYMENT', 'BANK_TRANSFER', 'CASH') NOT NULL,
    payment_status ENUM('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED') DEFAULT 'PENDING',
    transaction_id VARCHAR(100),
    payment_date TIMESTAMP,
    receipt_url VARCHAR(255),
    FOREIGN KEY (booking_id) REFERENCES bookings(id)
);

-- Notifications
CREATE TABLE notifications (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    type ENUM('BOOKING_CONFIRMATION', 'PAYMENT_RECEIPT', 'REMINDER', 'CANCELLATION') NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    is_read BOOLEAN DEFAULT FALSE,
    sent_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- System Configuration
CREATE TABLE system_config (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    config_key VARCHAR(100) UNIQUE NOT NULL,
    config_value TEXT,
    description TEXT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Indexes for Performance

```sql
-- Performance optimization indexes
CREATE INDEX idx_bookings_dates ON bookings(check_in_date, check_out_date);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_rooms_status ON rooms(status);
CREATE INDEX idx_payments_status ON payments(payment_status);
CREATE INDEX idx_notifications_user ON notifications(user_id, is_read);
```

---

## 🔌 API Specifications

### RESTful API Endpoints

#### Authentication Endpoints
```
POST   /api/auth/register          - User registration
POST   /api/auth/login             - User login
POST   /api/auth/logout            - User logout
POST   /api/auth/refresh           - Refresh JWT token
GET    /api/auth/profile           - Get user profile
PUT    /api/auth/profile           - Update user profile
```

#### Room Management Endpoints
```
GET    /api/rooms                  - List all rooms
GET    /api/rooms/{id}             - Get room details
GET    /api/rooms/available        - Get available rooms
GET    /api/room-types             - List room types
POST   /api/rooms                  - Add new room (Admin)
PUT    /api/rooms/{id}             - Update room (Admin)
DELETE /api/rooms/{id}             - Delete room (Admin)
```

#### Booking Endpoints
```
GET    /api/bookings               - List user bookings
GET    /api/bookings/{id}          - Get booking details
POST   /api/bookings               - Create new booking
PUT    /api/bookings/{id}          - Update booking
DELETE /api/bookings/{id}          - Cancel booking
GET    /api/bookings/check-availability - Check room availability
```

#### Payment Endpoints
```
GET    /api/payments               - List payments
GET    /api/payments/{id}          - Get payment details
POST   /api/payments               - Process payment
POST   /api/payments/refund        - Process refund
GET    /api/payments/invoice/{id}  - Generate invoice
```

#### Admin Endpoints
```
GET    /api/admin/dashboard        - Admin dashboard data
GET    /api/admin/reports          - Generate reports
GET    /api/admin/users            - List all users
PUT    /api/admin/users/{id}       - Update user
GET    /api/admin/bookings         - List all bookings
PUT    /api/admin/bookings/{id}    - Update booking status
```

### API Request/Response Examples

#### Booking Creation Request
```json
{
  "roomId": 101,
  "checkInDate": "2024-12-25",
  "checkOutDate": "2024-12-27",
  "guestCount": 2,
  "specialRequests": "Early check-in preferred"
}
```

#### Booking Response
```json
{
  "id": 12345,
  "bookingReference": "GP20241225001",
  "room": {
    "id": 101,
    "roomNumber": "101",
    "roomType": "Deluxe Room"
  },
  "checkInDate": "2024-12-25",
  "checkOutDate": "2024-12-27",
  "totalAmount": 45000.00,
  "status": "CONFIRMED",
  "createdAt": "2024-12-20T10:30:00Z"
}
```

---

## 🔒 Security Framework

### Authentication & Authorization

#### JWT Token Structure
```java
// JWT Payload
{
  "sub": "user123",
  "username": "chaminda.perera",
  "role": "ADMIN",
  "iat": 1640995200,
  "exp": 1641081600
}
```

#### Security Configuration
```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/payments/**").hasAnyRole("ADMIN", "PAYMENT_OFFICER")
                .requestMatchers("/api/bookings/**").authenticated()
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
}
```

### Data Protection

#### Password Hashing
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
}
```

#### Input Validation
```java
@Validated
public class BookingRequest {
    @NotNull
    @Future
    private LocalDate checkInDate;
    
    @NotNull
    @Future
    private LocalDate checkOutDate;
    
    @Min(1)
    @Max(10)
    private Integer guestCount;
}
```

---

## 🎨 User Interface Design

### Design System

#### Color Palette
- **Primary:** #1E3A8A (Deep Blue)
- **Secondary:** #F59E0B (Golden Yellow)
- **Success:** #10B981 (Emerald Green)
- **Warning:** #F59E0B (Amber)
- **Error:** #EF4444 (Red)
- **Neutral:** #6B7280 (Gray)

#### Typography
- **Primary Font:** Inter, sans-serif
- **Secondary Font:** Georgia, serif
- **Heading Sizes:** 2rem, 1.5rem, 1.25rem, 1rem
- **Body Text:** 1rem, 0.875rem

### Responsive Design Breakpoints
```css
/* Mobile First Approach */
@media (min-width: 640px) { /* Small devices */ }
@media (min-width: 768px) { /* Medium devices */ }
@media (min-width: 1024px) { /* Large devices */ }
@media (min-width: 1280px) { /* Extra large devices */ }
```

### Key UI Components

#### Navigation Structure
```
├── Guest Portal
│   ├── Home
│   ├── Room Search
│   ├── Event Spaces
│   ├── My Bookings
│   └── Profile
├── Admin Dashboard
│   ├── Overview
│   ├── Bookings Management
│   ├── Room Management
│   ├── User Management
│   ├── Reports
│   └── System Settings
└── Staff Portal
    ├── Check-in/Check-out
    ├── Guest Management
    └── Payment Processing
```

### Wireframe Specifications

#### Homepage Layout
```
┌─────────────────────────────────────────────────────────┐
│                    Header Navigation                      │
├─────────────────────────────────────────────────────────┤
│  Hero Section with Search Form                          │
│  ┌─────────────────────────────────────────────────────┐ │
│  │ Check-in: [Date] Check-out: [Date] Guests: [Count]  │ │
│  │ [Search Availability]                                │ │
│  └─────────────────────────────────────────────────────┘ │
├─────────────────────────────────────────────────────────┤
│  Featured Rooms & Event Spaces                          │
├─────────────────────────────────────────────────────────┤
│  Special Offers & Packages                              │
├─────────────────────────────────────────────────────────┤
│  Testimonials & Reviews                                 │
├─────────────────────────────────────────────────────────┤
│                    Footer                                │
└─────────────────────────────────────────────────────────┘
```

---

## 🧠 Business Logic Implementation

### Booking Workflow

```java
@Service
@Transactional
public class BookingService {
    
    public BookingResponse createBooking(BookingRequest request) {
        // 1. Validate request
        validateBookingRequest(request);
        
        // 2. Check availability
        if (!isRoomAvailable(request.getRoomId(), request.getCheckInDate(), request.getCheckOutDate())) {
            throw new RoomNotAvailableException("Room is not available for selected dates");
        }
        
        // 3. Calculate pricing
        BigDecimal totalAmount = calculateTotalAmount(request);
        
        // 4. Create booking
        Booking booking = new Booking();
        booking.setBookingReference(generateBookingReference());
        booking.setRoomId(request.getRoomId());
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setTotalAmount(totalAmount);
        booking.setStatus(BookingStatus.PENDING);
        
        // 5. Save booking
        booking = bookingRepository.save(booking);
        
        // 6. Send confirmation notification
        notificationService.sendBookingConfirmation(booking);
        
        return BookingResponse.from(booking);
    }
}
```

### Payment Processing

```java
@Service
public class PaymentService {
    
    public PaymentResponse processPayment(PaymentRequest request) {
        // 1. Validate payment request
        validatePaymentRequest(request);
        
        // 2. Process payment based on method
        PaymentResult result = switch (request.getPaymentMethod()) {
            case CREDIT_CARD, DEBIT_CARD -> processCardPayment(request);
            case QR_PAYMENT -> processQRPayment(request);
            case BANK_TRANSFER -> processBankTransfer(request);
            case CASH -> processCashPayment(request);
        };
        
        // 3. Update booking status
        if (result.isSuccess()) {
            updateBookingStatus(request.getBookingId(), BookingStatus.CONFIRMED);
            notificationService.sendPaymentConfirmation(result.getPayment());
        }
        
        return PaymentResponse.from(result);
    }
}
```

### Availability Management

```java
@Service
public class AvailabilityService {
    
    public boolean isRoomAvailable(Long roomId, LocalDate checkIn, LocalDate checkOut) {
        List<Booking> conflictingBookings = bookingRepository
            .findConflictingBookings(roomId, checkIn, checkOut);
        
        return conflictingBookings.isEmpty();
    }
    
    public List<Room> getAvailableRooms(LocalDate checkIn, LocalDate checkOut, Integer guestCount) {
        return roomRepository.findAvailableRooms(checkIn, checkOut, guestCount);
    }
}
```

---

## 🔗 Integration Requirements

### External API Integrations

#### Payment Gateway Integration
```java
@Service
public class PaymentGatewayService {
    
    public PaymentResult processCardPayment(CardPaymentRequest request) {
        // Integration with payment gateway (e.g., Stripe, PayPal)
        PaymentGatewayResponse response = paymentGatewayClient.processPayment(request);
        
        if (response.isSuccessful()) {
            return PaymentResult.success(response.getTransactionId());
        } else {
            return PaymentResult.failure(response.getErrorMessage());
        }
    }
}
```

#### Email Service Integration
```java
@Service
public class EmailService {
    
    public void sendBookingConfirmation(Booking booking) {
        EmailRequest emailRequest = EmailRequest.builder()
            .to(booking.getUser().getEmail())
            .subject("Booking Confirmation - Golden Palm Resort")
            .template("booking-confirmation")
            .data(Map.of(
                "bookingReference", booking.getBookingReference(),
                "checkInDate", booking.getCheckInDate(),
                "totalAmount", booking.getTotalAmount()
            ))
            .build();
        
        emailClient.sendEmail(emailRequest);
    }
}
```

#### SMS Service Integration (Optional)
```java
@Service
public class SMSService {
    
    public void sendReminder(Booking booking) {
        SMSRequest smsRequest = SMSRequest.builder()
            .to(booking.getUser().getPhone())
            .message("Reminder: Your booking at Golden Palm Resort is tomorrow. " +
                    "Check-in time: 2:00 PM")
            .build();
        
        smsClient.sendSMS(smsRequest);
    }
}
```

---

## 🧪 Testing Strategy

### Testing Pyramid

```
        /\
       /  \     E2E Tests (10%)
      /____\    
     /      \   Integration Tests (20%)
    /________\  
   /          \ Unit Tests (70%)
  /____________\
```

### Unit Testing

```java
@ExtendWith(MockitoExtension.class)
class BookingServiceTest {
    
    @Mock
    private BookingRepository bookingRepository;
    
    @Mock
    private RoomRepository roomRepository;
    
    @InjectMocks
    private BookingService bookingService;
    
    @Test
    void createBooking_WithValidRequest_ShouldCreateBooking() {
        // Given
        BookingRequest request = createValidBookingRequest();
        when(roomRepository.isAvailable(any(), any(), any())).thenReturn(true);
        when(bookingRepository.save(any())).thenReturn(createMockBooking());
        
        // When
        BookingResponse response = bookingService.createBooking(request);
        
        // Then
        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isEqualTo("CONFIRMED");
        verify(bookingRepository).save(any(Booking.class));
    }
}
```

### Integration Testing

```java
@SpringBootTest
@AutoConfigureTestDatabase
class BookingIntegrationTest {
    
    @Autowired
    private BookingController bookingController;
    
    @Autowired
    private TestEntityManager entityManager;
    
    @Test
    void createBooking_EndToEnd_ShouldWork() {
        // Given
        BookingRequest request = createBookingRequest();
        
        // When
        ResponseEntity<BookingResponse> response = bookingController.createBooking(request);
        
        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().getBookingReference()).isNotNull();
    }
}
```

---

## 🚀 Deployment & DevOps

### Environment Configuration

#### Application Properties
```properties
# Database Configuration
spring.datasource.url=jdbc:mysql://localhost:3306/goldenpalm_resort
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# JWT Configuration
jwt.secret=${JWT_SECRET}
jwt.expiration=86400000

# Email Configuration
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=${EMAIL_USERNAME}
spring.mail.password=${EMAIL_PASSWORD}

# Payment Gateway Configuration
payment.gateway.api.key=${PAYMENT_API_KEY}
payment.gateway.secret=${PAYMENT_SECRET}
```

### Docker Configuration

```dockerfile
FROM openjdk:17-jdk-slim

WORKDIR /app

COPY target/goldenpalm-resort-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Deployment Pipeline

```yaml
# GitHub Actions Workflow
name: Deploy Golden Palm Resort

on:
  push:
    branches: [main]

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v2
    
    - name: Set up JDK 17
      uses: actions/setup-java@v2
      with:
        java-version: '17'
        distribution: 'adopt'
    
    - name: Build with Maven
      run: mvn clean package
    
    - name: Run tests
      run: mvn test
    
    - name: Deploy to server
      run: |
        # Deployment commands
```

---

## ⚡ Performance & Scalability

### Performance Benchmarks

#### Response Time Targets
- **Page Load:** < 2 seconds
- **API Response:** < 500ms
- **Database Queries:** < 100ms
- **Payment Processing:** < 3 seconds

#### Concurrent User Capacity
- **Target:** 1000 concurrent users
- **Peak Load:** 2000 concurrent users
- **Database Connections:** 50-100

### Caching Strategy

```java
@Configuration
@EnableCaching
public class CacheConfig {
    
    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
            "rooms", "roomTypes", "bookings", "users"
        );
    }
}

@Service
public class RoomService {
    
    @Cacheable("rooms")
    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }
    
    @CacheEvict("rooms")
    public void updateRoom(Room room) {
        roomRepository.save(room);
    }
}
```

### Database Optimization

```sql
-- Query optimization
EXPLAIN SELECT r.* FROM rooms r
LEFT JOIN bookings b ON r.id = b.room_id
WHERE r.status = 'AVAILABLE'
AND (b.id IS NULL OR 
     (b.check_out_date < '2024-12-25' OR b.check_in_date > '2024-12-27'));

-- Index optimization
CREATE INDEX idx_booking_dates ON bookings(check_in_date, check_out_date);
CREATE INDEX idx_room_status ON rooms(status);
```

---

## 🔧 Maintenance & Support

### Monitoring & Logging

```java
@Aspect
@Component
public class LoggingAspect {
    
    private static final Logger logger = LoggerFactory.getLogger(LoggingAspect.class);
    
    @Around("@annotation(org.springframework.web.bind.annotation.RequestMapping)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        Object result = joinPoint.proceed();
        
        long endTime = System.currentTimeMillis();
        logger.info("Method {} executed in {} ms", 
                   joinPoint.getSignature().getName(), 
                   endTime - startTime);
        
        return result;
    }
}
```

### Health Checks

```java
@Component
public class DatabaseHealthIndicator implements HealthIndicator {
    
    @Autowired
    private DataSource dataSource;
    
    @Override
    public Health health() {
        try (Connection connection = dataSource.getConnection()) {
            if (connection.isValid(1000)) {
                return Health.up().build();
            }
        } catch (SQLException e) {
            return Health.down().withException(e).build();
        }
        return Health.down().build();
    }
}
```

### Backup Strategy

```bash
#!/bin/bash
# Database backup script

DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_DIR="/backups"
DB_NAME="goldenpalm_resort"

mysqldump -u root -p $DB_NAME > $BACKUP_DIR/backup_$DATE.sql

# Keep only last 7 days of backups
find $BACKUP_DIR -name "backup_*.sql" -mtime +7 -delete
```

---

## 📋 Implementation Timeline

### Phase 1: Foundation (Weeks 1-2)
- [ ] Project setup and configuration
- [ ] Database schema implementation
- [ ] Basic authentication system
- [ ] User management module

### Phase 2: Core Features (Weeks 3-4)
- [ ] Room management system
- [ ] Booking functionality
- [ ] Basic payment integration
- [ ] Frontend templates

### Phase 3: Advanced Features (Weeks 5-6)
- [ ] Event space management
- [ ] Advanced payment methods
- [ ] Notification system
- [ ] Reporting module

### Phase 4: Testing & Deployment (Weeks 7-8)
- [ ] Comprehensive testing
- [ ] Performance optimization
- [ ] Security audit
- [ ] Production deployment

---

## 🎯 Success Metrics

### Technical Metrics
- **System Uptime:** > 99.5%
- **Response Time:** < 500ms average
- **Error Rate:** < 0.1%
- **Security Incidents:** 0

### Business Metrics
- **Booking Conversion Rate:** > 15%
- **User Satisfaction:** > 4.5/5
- **Payment Success Rate:** > 98%
- **Support Ticket Resolution:** < 24 hours

---

## 📞 Support & Contact

**Project Team:** SLIIT 2025-Y2-S1-MLB-B1G2-02  
**Technical Lead:** [To be assigned]  
**Project Manager:** [To be assigned]  
**Support Email:** support@goldenpalmresort.com  

---

*This document serves as the comprehensive technical specification for the Golden Palm Resort Hotel Reservation System. All development should adhere to the standards and requirements outlined herein.* 