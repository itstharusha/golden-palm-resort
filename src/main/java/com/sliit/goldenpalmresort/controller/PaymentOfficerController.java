package com.sliit.goldenpalmresort.controller;

import com.sliit.goldenpalmresort.dto.PaymentResponse;
import com.sliit.goldenpalmresort.model.Payment;
import com.sliit.goldenpalmresort.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment-officer")
public class PaymentOfficerController {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    // Get all payments
    @GetMapping("/payments")
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        try {
            System.out.println("Getting all payments...");
            List<Payment> payments = paymentRepository.findAll();
            System.out.println("Found " + payments.size() + " payments");
            
            List<PaymentResponse> paymentResponses = new ArrayList<>();
            for (Payment payment : payments) {
                try {
                    PaymentResponse response = new PaymentResponse(payment);
                    paymentResponses.add(response);
                } catch (Exception e) {
                    System.err.println("Error processing payment " + payment.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("Returning " + paymentResponses.size() + " payment responses");
            return ResponseEntity.ok(paymentResponses);
        } catch (Exception e) {
            System.err.println("Error in getAllPayments: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Get payment by ID
    @GetMapping("/payments/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        try {
            System.out.println("Getting payment with ID: " + id);
            Payment payment = paymentRepository.findById(id).orElse(null);
            
            if (payment == null) {
                System.out.println("Payment not found with ID: " + id);
                return ResponseEntity.notFound().build();
            }
            
            PaymentResponse response = new PaymentResponse(payment);
            System.out.println("Returning payment response for ID: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error in getPaymentById: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Update payment status
    @PutMapping("/payments/{id}/status")
    public ResponseEntity<Map<String, Object>> updatePaymentStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        try {
            System.out.println("Updating payment status for ID: " + id);
            Payment payment = paymentRepository.findById(id).orElse(null);
            
            if (payment == null) {
                System.out.println("Payment not found with ID: " + id);
                return ResponseEntity.notFound().build();
            }
            
            String newStatus = request.get("status");
            String processedBy = request.get("processedBy");
            String notes = request.get("notes");
            
            if (newStatus != null) {
                try {
                    Payment.PaymentStatus status = Payment.PaymentStatus.valueOf(newStatus.toUpperCase());
                    payment.setPaymentStatus(status);
                    
                    if (status == Payment.PaymentStatus.COMPLETED && payment.getPaymentDate() == null) {
                        payment.setPaymentDate(LocalDateTime.now());
                    }
                    
                    if (processedBy != null) {
                        payment.setProcessedBy(processedBy);
                    }
                    
                    if (notes != null) {
                        payment.setNotes(notes);
                    }
                    
                    paymentRepository.save(payment);
                    
                    Map<String, Object> response = new HashMap<>();
                    response.put("message", "Payment status updated successfully");
                    response.put("paymentId", id);
                    response.put("newStatus", status.name());
                    
                    System.out.println("Payment status updated successfully for ID: " + id);
                    return ResponseEntity.ok(response);
                } catch (IllegalArgumentException e) {
                    System.err.println("Invalid payment status: " + newStatus);
                    return ResponseEntity.badRequest().body(Map.of("error", "Invalid payment status"));
                }
            } else {
                System.err.println("Status not provided in request");
                return ResponseEntity.badRequest().body(Map.of("error", "Status is required"));
            }
        } catch (Exception e) {
            System.err.println("Error in updatePaymentStatus: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Process refund
    @PostMapping("/payments/{id}/refund")
    public ResponseEntity<Map<String, Object>> processRefund(
            @PathVariable Long id,
            @RequestBody Map<String, Object> request) {
        try {
            System.out.println("Processing refund for payment ID: " + id);
            Payment payment = paymentRepository.findById(id).orElse(null);
            
            if (payment == null) {
                System.out.println("Payment not found with ID: " + id);
                return ResponseEntity.notFound().build();
            }
            
            BigDecimal refundAmount = new BigDecimal(request.get("refundAmount").toString());
            String refundReason = (String) request.get("refundReason");
            String processedBy = (String) request.get("processedBy");
            
            if (refundAmount.compareTo(BigDecimal.ZERO) <= 0) {
                System.err.println("Invalid refund amount: " + refundAmount);
                return ResponseEntity.badRequest().body(Map.of("error", "Refund amount must be greater than 0"));
            }
            
            if (refundAmount.compareTo(payment.getAmount()) > 0) {
                System.err.println("Refund amount exceeds payment amount");
                return ResponseEntity.badRequest().body(Map.of("error", "Refund amount cannot exceed payment amount"));
            }
            
            payment.setRefundAmount(refundAmount);
            payment.setRefundReason(refundReason);
            payment.setRefundDate(LocalDateTime.now());
            
            if (refundAmount.compareTo(payment.getAmount()) == 0) {
                payment.setPaymentStatus(Payment.PaymentStatus.REFUNDED);
            } else {
                payment.setPaymentStatus(Payment.PaymentStatus.PARTIALLY_REFUNDED);
            }
            
            if (processedBy != null) {
                payment.setProcessedBy(processedBy);
            }
            
            paymentRepository.save(payment);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Refund processed successfully");
            response.put("paymentId", id);
            response.put("refundAmount", refundAmount);
            response.put("newStatus", payment.getPaymentStatus().name());
            
            System.out.println("Refund processed successfully for payment ID: " + id);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Error in processRefund: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Get payments by status
    @GetMapping("/payments/status/{status}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(@PathVariable String status) {
        try {
            System.out.println("Getting payments with status: " + status);
            Payment.PaymentStatus paymentStatus = Payment.PaymentStatus.valueOf(status.toUpperCase());
            List<Payment> payments = paymentRepository.findByPaymentStatus(paymentStatus);
            
            List<PaymentResponse> paymentResponses = new ArrayList<>();
            for (Payment payment : payments) {
                try {
                    PaymentResponse response = new PaymentResponse(payment);
                    paymentResponses.add(response);
                } catch (Exception e) {
                    System.err.println("Error processing payment " + payment.getId() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
            
            System.out.println("Returning " + paymentResponses.size() + " payments with status: " + status);
            return ResponseEntity.ok(paymentResponses);
        } catch (IllegalArgumentException e) {
            System.err.println("Invalid payment status: " + status);
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.err.println("Error in getPaymentsByStatus: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Get daily payment summary
    @GetMapping("/reports/daily")
    public ResponseEntity<Map<String, Object>> getDailyPaymentReport(@RequestParam(required = false) String date) {
        try {
            System.out.println("Getting daily payment report for date: " + date);
            LocalDate reportDate = date != null ? LocalDate.parse(date) : LocalDate.now();
            LocalDateTime startOfDay = reportDate.atStartOfDay();
            LocalDateTime endOfDay = reportDate.atTime(LocalTime.MAX);
            
            List<Payment> dailyPayments = paymentRepository.findByPaymentDateBetween(startOfDay, endOfDay);
            BigDecimal totalAmount = paymentRepository.sumCompletedAmountByDateRange(startOfDay, endOfDay);
            
            Map<String, Object> report = new HashMap<>();
            report.put("date", reportDate.toString());
            report.put("totalPayments", dailyPayments.size());
            report.put("totalAmount", totalAmount);
            report.put("completedPayments", paymentRepository.countByPaymentStatus(Payment.PaymentStatus.COMPLETED));
            report.put("pendingPayments", paymentRepository.countByPaymentStatus(Payment.PaymentStatus.PENDING));
            report.put("failedPayments", paymentRepository.countByPaymentStatus(Payment.PaymentStatus.FAILED));
            report.put("refundedPayments", paymentRepository.countByPaymentStatus(Payment.PaymentStatus.REFUNDED));
            
            System.out.println("Daily payment report generated for date: " + reportDate);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            System.err.println("Error in getDailyPaymentReport: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Get monthly revenue report
    @GetMapping("/reports/monthly")
    public ResponseEntity<Map<String, Object>> getMonthlyRevenueReport(@RequestParam(required = false) String month) {
        try {
            System.out.println("Getting monthly revenue report for month: " + month);
            LocalDate reportMonth = month != null ? LocalDate.parse(month + "-01") : LocalDate.now().withDayOfMonth(1);
            LocalDateTime startOfMonth = reportMonth.atStartOfDay();
            LocalDateTime endOfMonth = reportMonth.plusMonths(1).minusDays(1).atTime(LocalTime.MAX);
            
            BigDecimal monthlyRevenue = paymentRepository.sumCompletedAmountByDateRange(startOfMonth, endOfMonth);
            List<Payment> monthlyPayments = paymentRepository.findByPaymentDateBetween(startOfMonth, endOfMonth);
            
            Map<String, Object> report = new HashMap<>();
            report.put("month", reportMonth.getMonth().toString() + " " + reportMonth.getYear());
            report.put("totalRevenue", monthlyRevenue);
            report.put("totalPayments", monthlyPayments.size());
            report.put("averagePayment", monthlyPayments.isEmpty() ? BigDecimal.ZERO : monthlyRevenue.divide(BigDecimal.valueOf(monthlyPayments.size()), 2, BigDecimal.ROUND_HALF_UP));
            
            System.out.println("Monthly revenue report generated for month: " + reportMonth);
            return ResponseEntity.ok(report);
        } catch (Exception e) {
            System.err.println("Error in getMonthlyRevenueReport: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
    
    // Get payment statistics
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getPaymentStatistics() {
        try {
            System.out.println("Getting payment statistics...");
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalPayments", paymentRepository.count());
            statistics.put("completedPayments", paymentRepository.countByPaymentStatus(Payment.PaymentStatus.COMPLETED));
            statistics.put("pendingPayments", paymentRepository.countByPaymentStatus(Payment.PaymentStatus.PENDING));
            statistics.put("failedPayments", paymentRepository.countByPaymentStatus(Payment.PaymentStatus.FAILED));
            statistics.put("refundedPayments", paymentRepository.countByPaymentStatus(Payment.PaymentStatus.REFUNDED));
            statistics.put("totalRevenue", paymentRepository.sumAmountByStatus(Payment.PaymentStatus.COMPLETED));
            statistics.put("totalRefunds", paymentRepository.sumAmountByStatus(Payment.PaymentStatus.REFUNDED));
            
            System.out.println("Payment statistics generated successfully");
            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            System.err.println("Error in getPaymentStatistics: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
} 