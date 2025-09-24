package com.sliit.goldenpalmresort.dto;

import com.sliit.goldenpalmresort.model.EventBooking;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class EventBookingResponse {
    
    private Long id;
    private String bookingReference;
    private EventSpaceInfo eventSpace;
    private String eventType;
    private LocalDate eventDate;
    private String startTime;
    private String endTime;
    private Integer expectedGuests;
    private BigDecimal totalAmount;
    private String status;
    private String setupRequirements;
    private boolean cateringRequired;
    private boolean audioVisualRequired;
    private String specialRequests;
    private String contactPerson;
    private String contactPhone;
    private String contactEmail;
    private LocalDateTime createdAt;
    
    public static class EventSpaceInfo {
        private Long id;
        private String name;
        private String description;
        private Integer capacity;
        private BigDecimal basePrice;
        private String setupTypes;
        private String amenities;
        private String dimensions;
        private boolean cateringAvailable;
        private boolean audioVisualEquipment;
        private boolean parkingAvailable;
        
        public EventSpaceInfo() {}
        
        public EventSpaceInfo(Long id, String name, String description, Integer capacity, 
                            BigDecimal basePrice, String setupTypes, String amenities, 
                            String dimensions, boolean cateringAvailable, 
                            boolean audioVisualEquipment, boolean parkingAvailable) {
            this.id = id;
            this.name = name;
            this.description = description;
            this.capacity = capacity;
            this.basePrice = basePrice;
            this.setupTypes = setupTypes;
            this.amenities = amenities;
            this.dimensions = dimensions;
            this.cateringAvailable = cateringAvailable;
            this.audioVisualEquipment = audioVisualEquipment;
            this.parkingAvailable = parkingAvailable;
        }
        
        // Getters and Setters
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public Integer getCapacity() { return capacity; }
        public void setCapacity(Integer capacity) { this.capacity = capacity; }
        
        public BigDecimal getBasePrice() { return basePrice; }
        public void setBasePrice(BigDecimal basePrice) { this.basePrice = basePrice; }
        
        public String getSetupTypes() { return setupTypes; }
        public void setSetupTypes(String setupTypes) { this.setupTypes = setupTypes; }
        
        public String getAmenities() { return amenities; }
        public void setAmenities(String amenities) { this.amenities = amenities; }
        
        public String getDimensions() { return dimensions; }
        public void setDimensions(String dimensions) { this.dimensions = dimensions; }
        
        public boolean isCateringAvailable() { return cateringAvailable; }
        public void setCateringAvailable(boolean cateringAvailable) { this.cateringAvailable = cateringAvailable; }
        
        public boolean isAudioVisualEquipment() { return audioVisualEquipment; }
        public void setAudioVisualEquipment(boolean audioVisualEquipment) { this.audioVisualEquipment = audioVisualEquipment; }
        
        public boolean isParkingAvailable() { return parkingAvailable; }
        public void setParkingAvailable(boolean parkingAvailable) { this.parkingAvailable = parkingAvailable; }
    }
    
    // Constructors
    public EventBookingResponse() {}
    
    public EventBookingResponse(Long id, String bookingReference, EventSpaceInfo eventSpace, 
                               String eventType, LocalDate eventDate, String startTime, String endTime,
                               Integer expectedGuests, BigDecimal totalAmount, String status, 
                               LocalDateTime createdAt) {
        this.id = id;
        this.bookingReference = bookingReference;
        this.eventSpace = eventSpace;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.expectedGuests = expectedGuests;
        this.totalAmount = totalAmount;
        this.status = status;
        this.createdAt = createdAt;
    }
    
    public static EventBookingResponse from(EventBooking booking) {
        EventBookingResponse response = new EventBookingResponse();
        response.setId(booking.getId());
        response.setBookingReference(booking.getBookingReference());
        response.setEventType(booking.getEventType());
        response.setEventDate(booking.getEventDate());
        response.setStartTime(booking.getStartTime());
        response.setEndTime(booking.getEndTime());
        response.setExpectedGuests(booking.getExpectedGuests());
        response.setTotalAmount(booking.getTotalAmount());
        response.setStatus(booking.getStatus().name());
        response.setSetupRequirements(booking.getSetupRequirements());
        response.setCateringRequired(booking.isCateringRequired());
        response.setAudioVisualRequired(booking.isAudioVisualRequired());
        response.setSpecialRequests(booking.getSpecialRequests());
        response.setContactPerson(booking.getContactPerson());
        response.setContactPhone(booking.getContactPhone());
        response.setContactEmail(booking.getContactEmail());
        response.setCreatedAt(booking.getCreatedAt());
        
        if (booking.getEventSpace() != null) {
            EventSpaceInfo eventSpaceInfo = new EventSpaceInfo();
            eventSpaceInfo.setId(booking.getEventSpace().getId());
            eventSpaceInfo.setName(booking.getEventSpace().getName());
            eventSpaceInfo.setDescription(booking.getEventSpace().getDescription());
            eventSpaceInfo.setCapacity(booking.getEventSpace().getCapacity());
            eventSpaceInfo.setBasePrice(booking.getEventSpace().getBasePrice());
            eventSpaceInfo.setSetupTypes(booking.getEventSpace().getSetupTypes());
            eventSpaceInfo.setAmenities(booking.getEventSpace().getAmenities());
            eventSpaceInfo.setDimensions(booking.getEventSpace().getDimensions());
            eventSpaceInfo.setCateringAvailable(booking.getEventSpace().isCateringAvailable());
            eventSpaceInfo.setAudioVisualEquipment(booking.getEventSpace().isAudioVisualEquipment());
            eventSpaceInfo.setParkingAvailable(booking.getEventSpace().isParkingAvailable());
            response.setEventSpace(eventSpaceInfo);
        }
        
        return response;
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getBookingReference() { return bookingReference; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }
    
    public EventSpaceInfo getEventSpace() { return eventSpace; }
    public void setEventSpace(EventSpaceInfo eventSpace) { this.eventSpace = eventSpace; }
    
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
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
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
} 