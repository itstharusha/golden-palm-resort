package com.sliit.goldenpalmresort.dto;

import com.sliit.goldenpalmresort.model.Booking;
import com.sliit.goldenpalmresort.model.User;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class BookingResponse {
    private Long id;
    private String bookingReference;
    private Long userId;
    private String userName;
    private Long roomId;
    private String roomNumber;
    private String roomType;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer guestCount;
    private BigDecimal totalAmount;
    private String status;
    private String specialRequests;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long createdById;
    private String createdBy;

    // Getters
    public Long getId() { return id; }
    public String getBookingReference() { return bookingReference; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public Long getRoomId() { return roomId; }
    public String getRoomNumber() { return roomNumber; }
    public String getRoomType() { return roomType; }
    public LocalDate getCheckInDate() { return checkInDate; }
    public LocalDate getCheckOutDate() { return checkOutDate; }
    public Integer getGuestCount() { return guestCount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public String getSpecialRequests() { return specialRequests; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public Long getCreatedById() { return createdById; }
    public String getCreatedBy() { return createdBy; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setBookingReference(String bookingReference) { this.bookingReference = bookingReference; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setUserName(String userName) { this.userName = userName; }
    public void setRoomId(Long roomId) { this.roomId = roomId; }
    public void setRoomNumber(String roomNumber) { this.roomNumber = roomNumber; }
    public void setRoomType(String roomType) { this.roomType = roomType; }
    public void setCheckInDate(LocalDate checkInDate) { this.checkInDate = checkInDate; }
    public void setCheckOutDate(LocalDate checkOutDate) { this.checkOutDate = checkOutDate; }
    public void setGuestCount(Integer guestCount) { this.guestCount = guestCount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public void setStatus(String status) { this.status = status; }
    public void setSpecialRequests(String specialRequests) { this.specialRequests = specialRequests; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public void setCreatedById(Long createdById) { this.createdById = createdById; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }

    public static BookingResponse from(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setBookingReference(booking.getBookingReference());
        
        if (booking.getUser() != null) {
            response.setUserId(booking.getUser().getId());
            response.setUserName(booking.getUser().getFirstName() + " " + booking.getUser().getLastName());
        }
        
        if (booking.getRoom() != null) {
            response.setRoomId(booking.getRoom().getId());
            response.setRoomNumber(booking.getRoom().getRoomNumber());
            response.setRoomType(booking.getRoom().getRoomType());
        }
        
        response.setCheckInDate(booking.getCheckInDate());
        response.setCheckOutDate(booking.getCheckOutDate());
        response.setGuestCount(booking.getGuestCount());
        response.setTotalAmount(booking.getTotalAmount());
        response.setStatus(booking.getStatus().name());
        response.setSpecialRequests(booking.getSpecialRequests());
        response.setCreatedAt(booking.getCreatedAt());
        response.setUpdatedAt(booking.getUpdatedAt());
        
        if (booking.getCreatedBy() != null) {
            response.setCreatedById(booking.getCreatedBy().getId());
            response.setCreatedBy(booking.getCreatedBy().getFirstName() + " " + booking.getCreatedBy().getLastName());
        }
        
        return response;
    }
}