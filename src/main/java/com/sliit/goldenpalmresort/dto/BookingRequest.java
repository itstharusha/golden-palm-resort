package com.sliit.goldenpalmresort.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class BookingRequest {
    
    @NotNull(message = "Room ID is required")
    private Long roomId;
    
    @NotNull(message = "Check-in date is required")
    @Future(message = "Check-in date must be in the future")
    private LocalDate checkInDate;
    
    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in the future")
    private LocalDate checkOutDate;
    
    @NotNull(message = "Guest count is required")
    @Min(value = 1, message = "At least 1 guest is required")
    private Integer guestCount;
    
    private String specialRequests;
    
    // Guest information (for walk-in bookings or when booking for someone else)
    private String guestEmail;
    private String guestFirstName;
    private String guestLastName;
    private String guestPhone;
    
    // Payment information (if needed)
    private String paymentMethod;
    private String paymentReference;
    
    // Additional booking preferences
    private Boolean requireAirportPickup;
    private String flightNumber;
    private String specialAccommodations;
    
    // Constructors
    public BookingRequest() {}
    
    public BookingRequest(Long roomId, LocalDate checkInDate, LocalDate checkOutDate, Integer guestCount) {
        this.roomId = roomId;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.guestCount = guestCount;
    }

    // Getters
    public Long getRoomId() { return roomId; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public Integer getGuestCount() { return guestCount; }
    public String getSpecialRequests() { return specialRequests; }
    public String getGuestEmail() { return guestEmail; }
    public String getGuestFirstName() { return guestFirstName; }
    public String getGuestLastName() { return guestLastName; }
    public String getGuestPhone() { return guestPhone; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getPaymentReference() { return paymentReference; }
    public Boolean getRequireAirportPickup() { return requireAirportPickup; }
    public String getFlightNumber() { return flightNumber; }
    public String getSpecialAccommodations() { return specialAccommodations; }

    // Setters
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }
    public void setGuestCount(Integer guestCount) { this.guestCount = guestCount; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }
    public void setGuestEmail(String guestEmail) { this.guestEmail = guestEmail; }
    public void setGuestFirstName(String guestFirstName) { this.guestFirstName = guestFirstName; }
    public void setGuestLastName(String guestLastName) { this.guestLastName = guestLastName; }
    public void setGuestPhone(String guestPhone) { this.guestPhone = guestPhone; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    public void setPaymentReference(String paymentReference) { this.paymentReference = paymentReference; }
    public void setRequireAirportPickup(Boolean requireAirportPickup) { this.requireAirportPickup = requireAirportPickup; }
    public void setFlightNumber(String flightNumber) { this.flightNumber = flightNumber; }
    public void setSpecialAccommodations(String specialAccommodations) { this.specialAccommodations = specialAccommodations; }

    // Validation method
    public boolean isCheckOutAfterCheckIn() {
        if (checkInDate == null || checkOutDate == null) {
            return false;
        }
        return checkOutDate.isAfter(checkInDate);
    }
}