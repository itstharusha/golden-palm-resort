package com.sliit.goldenpalmresort.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class EventBookingRequest {
    
    @NotNull(message = "Event space ID is required")
    private Long eventSpaceId;
    
    @NotBlank(message = "Event type is required")
    private String eventType;
    
    @NotNull(message = "Event date is required")
    @Future(message = "Event date must be in the future")
    private LocalDate eventDate;
    
    @NotBlank(message = "Start time is required")
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "Start time must be in HH:MM format")
    private String startTime;
    
    @NotBlank(message = "End time is required")
    @Pattern(regexp = "^([01]?[0-9]|2[0-3]):[0-5][0-9]$", message = "End time must be in HH:MM format")
    private String endTime;
    
    @NotNull(message = "Expected guests is required")
    @Min(value = 1, message = "Expected guests must be at least 1")
    @Max(value = 1000, message = "Expected guests cannot exceed 1000")
    private Integer expectedGuests;
    
    private String setupRequirements;
    
    private boolean cateringRequired = false;
    
    private boolean audioVisualRequired = false;
    
    private String specialRequests;
    
    @NotBlank(message = "Contact person is required")
    private String contactPerson;
    
    @NotBlank(message = "Contact phone is required")
    private String contactPhone;
    
    @Email(message = "Contact email must be valid")
    private String contactEmail;
    
    // Constructors
    public EventBookingRequest() {}
    
    public EventBookingRequest(Long eventSpaceId, String eventType, LocalDate eventDate, 
                              String startTime, String endTime, Integer expectedGuests) {
        this.eventSpaceId = eventSpaceId;
        this.eventType = eventType;
        this.eventDate = eventDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.expectedGuests = expectedGuests;
    }
    
    // Getters and Setters
    public Long getEventSpaceId() { return eventSpaceId; }
    public void setEventSpaceId(Long eventSpaceId) { this.eventSpaceId = eventSpaceId; }
    
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
    
    // Validation method
    public boolean isEndTimeAfterStartTime() {
        if (startTime == null || endTime == null) {
            return false;
        }
        return endTime.compareTo(startTime) > 0;
    }
} 