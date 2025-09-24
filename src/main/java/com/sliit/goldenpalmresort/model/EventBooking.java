package com.sliit.goldenpalmresort.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "event_bookings")
public class EventBooking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "booking_reference", unique = true, nullable = false)
    private String bookingReference;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_space_id", nullable = false)
    private EventSpace eventSpace;
    
    @Column(name = "event_type", nullable = false)
    private String eventType; // Wedding, Conference, Meeting, Banquet, etc.
    
    @Column(name = "event_date", nullable = false)
    private LocalDate eventDate;
    
    @Column(name = "start_time", nullable = false)
    private String startTime; // e.g., "14:00"
    
    @Column(name = "end_time", nullable = false)
    private String endTime; // e.g., "22:00"
    
    @Column(name = "expected_guests", nullable = false)
    private Integer expectedGuests;
    
    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount;
    
    @Enumerated(EnumType.STRING)
    private EventBookingStatus status = EventBookingStatus.PENDING;
    
    @Column(name = "setup_requirements", columnDefinition = "TEXT")
    private String setupRequirements;
    
    @Column(name = "catering_required")
    private boolean cateringRequired = false;
    
    @Column(name = "audio_visual_required")
    private boolean audioVisualRequired = false;
    
    @Column(name = "special_requests", columnDefinition = "TEXT")
    private String specialRequests;
    
    @Column(name = "contact_person")
    private String contactPerson;
    
    @Column(name = "contact_phone")
    private String contactPhone;
    
    @Column(name = "contact_email")
    private String contactEmail;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public enum EventBookingStatus {
        PENDING, CONFIRMED, IN_PROGRESS, COMPLETED, CANCELLED
    }
    
    // Constructors
    public EventBooking() {}
    
    public EventBooking(User user, EventSpace eventSpace, String eventType, LocalDate eventDate, 
                       String startTime, String endTime, Integer expectedGuests) {
        this.user = user;
        this.eventSpace = eventSpace;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.expectedGuests = expectedGuests;
        this.bookingReference = generateBookingReference();
    }
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (bookingReference == null) {
            bookingReference = generateBookingReference();
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
    
    private String generateBookingReference() {
        return "EV" + LocalDateTime.now().getYear() + 
               String.format("%02d", LocalDateTime.now().getMonthValue()) +
               String.format("%02d", LocalDateTime.now().getDayOfMonth()) +
               String.format("%03d", (int)(Math.random() * 1000));
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }
    
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    
    public EventSpace getEventSpace() { return eventSpace; }
    public void setEventSpace(EventSpace eventSpace) { this.eventSpace = eventSpace; }
    
    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    
    public LocalDate getEventDate() { return eventDate; }
    public void setEventDate(LocalDate eventDate) { this.eventDate = eventDate; }
    
    public String getStartTime() { return startTime; }
    public void setStartTime(String startTime) { this.startTime = startTime; }
    
    public String getEndTime() { return endTime; }
    public void setEndTime(String endTime) { this.endTime = endTime; }
    
    public Integer getExpectedGuests() { return expectedGuests; }
    public void setExpectedGuests(Integer expectedGuests) { this.expectedGuests = expectedGuests; }
    
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    
    public EventBookingStatus getStatus() { return status; }
    public void setStatus(EventBookingStatus status) { this.status = status; }
    
    public String getSetupRequirements() { return setupRequirements; }
    public void setSetupRequirements(String setupRequirements) { this.setupRequirements = setupRequirements; }
    
    public boolean isCateringRequired() { return cateringRequired; }
    public void setCateringRequired(boolean cateringRequired) { this.cateringRequired = cateringRequired; }
    
    public boolean isAudioVisualRequired() { return audioVisualRequired; }
    public void setAudioVisualRequired(boolean audioVisualRequired) { this.audioVisualRequired = audioVisualRequired; }
    
    public String getSpecialRequests() { return specialRequests; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }
    
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String contactPerson) { this.contactPerson = contactPerson; }
    
    public String getContactPhone() { return contactPhone; }
    public void setContactPhone(String contactPhone) { this.contactPhone = contactPhone; }
    
    public String getContactEmail() { return contactEmail; }
    public void setContactEmail(String contactEmail) { this.contactEmail = contactEmail; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
} 