package com.sliit.goldenpalmresort.dto;

import com.sliit.goldenpalmresort.model.Payment;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class PaymentResponse {
    
    private Long id;
    private String bookingReference;
    private String guestName;
    private String roomOrEventName;
    private BigDecimal amount;
    private Payment.PaymentMethod paymentMethod;
    private Payment.PaymentStatus paymentStatus;
    private String transactionId;
    private LocalDateTime paymentDate;
    private String receiptUrl;
    private BigDecimal refundAmount;
    private String refundReason;
    private LocalDateTime refundDate;
    private String processedBy;
    private String notes;
    private LocalDateTime createdAt;
    private String paymentType; // "ROOM" or "EVENT"
    
    // Constructors
    public PaymentResponse() {}
    
    public PaymentResponse(Payment payment) {
        this.id = payment.getId();
        this.amount = payment.getAmount();
        this.paymentMethod = payment.getPaymentMethod();
        this.paymentStatus = payment.getPaymentStatus();
        this.transactionId = payment.getTransactionId();
        this.paymentDate = payment.getPaymentDate();
        this.receiptUrl = payment.getReceiptUrl();
        this.refundAmount = payment.getRefundAmount();
        this.refundReason = payment.getRefundReason();
        this.refundDate = payment.getRefundDate();
        this.processedBy = payment.getProcessedBy();
        this.notes = payment.getNotes();
        this.createdAt = payment.getCreatedAt();
        
        // Set booking reference and guest name
        if (payment.getBooking() != null) {
            this.bookingReference = payment.getBooking().getBookingReference();
            this.paymentType = "ROOM";
            if (payment.getBooking().getUser() != null) {
                this.guestName = payment.getBooking().getUser().getFirstName() + " " + payment.getBooking().getUser().getLastName();
            }
            if (payment.getBooking().getRoom() != null) {
                this.roomOrEventName = "Room " + payment.getBooking().getRoom().getRoomNumber();
            }
        } else if (payment.getEventBooking() != null) {
            this.bookingReference = payment.getEventBooking().getBookingReference();
            this.paymentType = "EVENT";
            if (payment.getEventBooking().getUser() != null) {
                this.guestName = payment.getEventBooking().getUser().getFirstName() + " " + payment.getEventBooking().getUser().getLastName();
            }
            if (payment.getEventBooking().getEventSpace() != null) {
                this.roomOrEventName = payment.getEventBooking().getEventSpace().getName();
            }
        }
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getBookingReference() {
        return bookingReference;
    }
    
    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }
    
    public String getGuestName() {
        return guestName;
    }
    
    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }
    
    public String getRoomOrEventName() {
        return roomOrEventName;
    }
    
    public void setRoomOrEventName(String roomOrEventName) {
        this.roomOrEventName = roomOrEventName;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public Payment.PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(Payment.PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public Payment.PaymentStatus getPaymentStatus() {
        return paymentStatus;
    }
    
    public void setPaymentStatus(Payment.PaymentStatus paymentStatus) {
        this.paymentStatus = paymentStatus;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }
    
    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }
    
    public String getReceiptUrl() {
        return receiptUrl;
    }
    
    public void setReceiptUrl(String receiptUrl) {
        this.receiptUrl = receiptUrl;
    }
    
    public BigDecimal getRefundAmount() {
        return refundAmount;
    }
    
    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }
    
    public String getRefundReason() {
        return refundReason;
    }
    
    public void setRefundReason(String refundReason) {
        this.refundReason = refundReason;
    }
    
    public LocalDateTime getRefundDate() {
        return refundDate;
    }
    
    public void setRefundDate(LocalDateTime refundDate) {
        this.refundDate = refundDate;
    }
    
    public String getProcessedBy() {
        return processedBy;
    }
    
    public void setProcessedBy(String processedBy) {
        this.processedBy = processedBy;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getPaymentType() {
        return paymentType;
    }
    
    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }
} 