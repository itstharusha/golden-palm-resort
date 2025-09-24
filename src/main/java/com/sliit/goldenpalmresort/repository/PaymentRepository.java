package com.sliit.goldenpalmresort.repository;

import com.sliit.goldenpalmresort.model.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    // Find payments by status
    List<Payment> findByPaymentStatus(Payment.PaymentStatus status);
    
    // Find payments by payment method
    List<Payment> findByPaymentMethod(Payment.PaymentMethod paymentMethod);
    
    // Find payments by booking
    List<Payment> findByBookingId(Long bookingId);
    
    // Find payments by event booking
    List<Payment> findByEventBookingId(Long eventBookingId);
    
    // Find payments by date range
    @Query("SELECT p FROM Payment p WHERE p.paymentDate BETWEEN :startDate AND :endDate")
    List<Payment> findByPaymentDateBetween(@Param("startDate") LocalDateTime startDate, 
                                          @Param("endDate") LocalDateTime endDate);
    
    // Find payments by amount range
    @Query("SELECT p FROM Payment p WHERE p.amount BETWEEN :minAmount AND :maxAmount")
    List<Payment> findByAmountBetween(@Param("minAmount") BigDecimal minAmount, 
                                     @Param("maxAmount") BigDecimal maxAmount);
    
    // Find completed payments by date range
    @Query("SELECT p FROM Payment p WHERE p.paymentStatus = 'COMPLETED' AND p.paymentDate BETWEEN :startDate AND :endDate")
    List<Payment> findCompletedPaymentsByDateRange(@Param("startDate") LocalDateTime startDate, 
                                                   @Param("endDate") LocalDateTime endDate);
    
    // Count payments by status
    long countByPaymentStatus(Payment.PaymentStatus status);
    
    // Sum total amount by status
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentStatus = :status")
    BigDecimal sumAmountByStatus(@Param("status") Payment.PaymentStatus status);
    
    // Sum total amount by date range
    @Query("SELECT COALESCE(SUM(p.amount), 0) FROM Payment p WHERE p.paymentStatus = 'COMPLETED' AND p.paymentDate BETWEEN :startDate AND :endDate")
    BigDecimal sumCompletedAmountByDateRange(@Param("startDate") LocalDateTime startDate, 
                                           @Param("endDate") LocalDateTime endDate);
    
    // Find refunded payments
    @Query("SELECT p FROM Payment p WHERE p.paymentStatus IN ('REFUNDED', 'PARTIALLY_REFUNDED')")
    List<Payment> findRefundedPayments();
    
    // Find payments by processed by
    List<Payment> findByProcessedBy(String processedBy);
    
    // Find payments with refunds
    @Query("SELECT p FROM Payment p WHERE p.refundAmount IS NOT NULL AND p.refundAmount > 0")
    List<Payment> findPaymentsWithRefunds();
} 