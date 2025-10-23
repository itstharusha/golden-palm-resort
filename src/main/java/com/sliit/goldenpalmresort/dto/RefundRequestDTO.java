package com.sliit.goldenpalmresort.dto;

import java.math.BigDecimal;

public class RefundRequestDTO {
    
    private String bookingReference;
    private String bookingType; // "ROOM" or "EVENT"
    private BigDecimal refundAmount;
    private String bankAccountNumber;
    private String bankName;
    private String bankBranch;
    private String accountHolderName;
    private String reason;
    
    // Constructors
    public RefundRequestDTO() {}
    
    // Getters and Setters
    public String getBookingReference() {
        return bookingReference;
    }
    
    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }
    
    public String getBookingType() {
        return bookingType;
    }
    
    public void setBookingType(String bookingType) {
        this.bookingType = bookingType;
    }
    
    public BigDecimal getRefundAmount() {
        return refundAmount;
    }
    
    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount;
    }
    
    public String getBankAccountNumber() {
        return bankAccountNumber;
    }
    
    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }
    
    public String getBankName() {
        return bankName;
    }
    
    public void setBankName(String bankName) {
        this.bankName = bankName;
    }
    
    public String getBankBranch() {
        return bankBranch;
    }
    
    public void setBankBranch(String bankBranch) {
        this.bankBranch = bankBranch;
    }
    
    public String getAccountHolderName() {
        return accountHolderName;
    }
    
    public void setAccountHolderName(String accountHolderName) {
        this.accountHolderName = accountHolderName;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
}
