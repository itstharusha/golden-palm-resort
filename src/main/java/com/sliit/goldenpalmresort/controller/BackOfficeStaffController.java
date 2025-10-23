package com.sliit.goldenpalmresort.controller;

import com.sliit.goldenpalmresort.model.*;
import com.sliit.goldenpalmresort.repository.*;
import com.sliit.goldenpalmresort.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/back-office")
@CrossOrigin(origins = "*")
public class BackOfficeStaffController {
    
    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private EventBookingRepository eventBookingRepository;
    
    @Autowired
    private RefundRequestRepository refundRequestRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    /**
     * Get all event bookings with pending payments (for payment reminders)
     */
    @GetMapping("/event-bookings/pending-payment")
    public ResponseEntity<List<Map<String, Object>>> getPendingPaymentEventBookings() {
        try {
            List<EventBooking> allEventBookings = eventBookingRepository.findAll();
            List<Map<String, Object>> pendingPayments = new ArrayList<>();
            
            for (EventBooking booking : allEventBookings) {
                // Check if payment exists and is not completed
                Optional<Payment> paymentOpt = paymentRepository.findByEventBooking(booking);
                
                boolean needsPaymentReminder = false;
                if (paymentOpt.isEmpty()) {
                    needsPaymentReminder = true;
                } else {
                    Payment payment = paymentOpt.get();
                    if (payment.getPaymentStatus() == Payment.PaymentStatus.PENDING ||
                        payment.getPaymentStatus() == Payment.PaymentStatus.FAILED) {
                        needsPaymentReminder = true;
                    }
                }
                
                if (needsPaymentReminder && booking.getStatus() != EventBooking.EventBookingStatus.CANCELLED) {
                    Map<String, Object> bookingData = new HashMap<>();
                    bookingData.put("id", booking.getId());
                    bookingData.put("bookingReference", booking.getBookingReference());
                    bookingData.put("eventType", booking.getEventType());
                    bookingData.put("eventDate", booking.getEventDate());
                    bookingData.put("totalAmount", booking.getTotalAmount());
                    bookingData.put("status", booking.getStatus());
                    bookingData.put("guestName", booking.getUser().getFirstName() + " " + booking.getUser().getLastName());
                    bookingData.put("guestEmail", booking.getUser().getEmail());
                    bookingData.put("userId", booking.getUser().getId());
                    bookingData.put("eventSpaceName", booking.getEventSpace().getName());
                    
                    pendingPayments.add(bookingData);
                }
            }
            
            return ResponseEntity.ok(pendingPayments);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get all approved refund requests (for refund approval notifications)
     */
    @GetMapping("/refunds/approved")
    public ResponseEntity<List<Map<String, Object>>> getApprovedRefunds() {
        try {
            List<RefundRequest> approvedRefunds = refundRequestRepository.findByStatus(RefundRequest.RefundStatus.APPROVED);
            
            List<Map<String, Object>> refundData = approvedRefunds.stream().map(refund -> {
                Map<String, Object> data = new HashMap<>();
                data.put("id", refund.getId());
                data.put("bookingReference", refund.getBookingReference());
                data.put("refundAmount", refund.getRefundAmount());
                data.put("status", refund.getStatus());
                data.put("processedAt", refund.getProcessedAt());
                data.put("guestName", refund.getUser().getFirstName() + " " + refund.getUser().getLastName());
                data.put("guestEmail", refund.getUser().getEmail());
                data.put("userId", refund.getUser().getId());
                data.put("bankAccountNumber", refund.getBankAccountNumber());
                data.put("bankName", refund.getBankName());
                
                return data;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(refundData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Send payment reminder notification
     */
    @PostMapping("/notifications/payment-reminder")
    public ResponseEntity<Map<String, Object>> sendPaymentReminder(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            Long eventBookingId = Long.valueOf(request.get("eventBookingId").toString());
            String customMessage = request.get("message") != null ? request.get("message").toString() : null;
            
            Optional<EventBooking> bookingOpt = eventBookingRepository.findById(eventBookingId);
            if (bookingOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Event booking not found"));
            }
            
            EventBooking booking = bookingOpt.get();
            User guest = booking.getUser();
            String staffUsername = authentication.getName();
            
            String title = "Payment Reminder - " + booking.getBookingReference();
            String message = customMessage != null ? customMessage :
                String.format("Dear %s, this is a reminder that payment for your event booking (%s) on %s is pending. " +
                             "Total amount: LKR %.2f. Please complete the payment at your earliest convenience.",
                             guest.getFirstName(), booking.getBookingReference(), 
                             booking.getEventDate(), booking.getTotalAmount());
            
            Notification notification = notificationService.createNotificationWithReference(
                guest,
                Notification.NotificationType.PAYMENT_REMINDER,
                title,
                message,
                staffUsername,
                booking.getId(),
                "EVENT_BOOKING"
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Payment reminder sent successfully");
            response.put("notificationId", notification.getId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to send payment reminder: " + e.getMessage()));
        }
    }
    
    /**
     * Send refund approval notification
     */
    @PostMapping("/notifications/refund-approved")
    public ResponseEntity<Map<String, Object>> sendRefundApprovedNotification(
            @RequestBody Map<String, Object> request,
            Authentication authentication) {
        try {
            Long refundRequestId = Long.valueOf(request.get("refundRequestId").toString());
            String customMessage = request.get("message") != null ? request.get("message").toString() : null;
            
            Optional<RefundRequest> refundOpt = refundRequestRepository.findById(refundRequestId);
            if (refundOpt.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Refund request not found"));
            }
            
            RefundRequest refund = refundOpt.get();
            User guest = refund.getUser();
            String staffUsername = authentication.getName();
            
            String title = "Refund Approved - " + refund.getBookingReference();
            String message = customMessage != null ? customMessage :
                String.format("Dear %s, your refund request for booking %s has been approved. " +
                             "Amount: LKR %.2f will be processed to your bank account (%s - %s) within 5-7 business days.",
                             guest.getFirstName(), refund.getBookingReference(), 
                             refund.getRefundAmount(), refund.getBankName(), 
                             refund.getBankAccountNumber().replaceAll("\\d(?=\\d{4})", "*"));
            
            Notification notification = notificationService.createNotificationWithReference(
                guest,
                Notification.NotificationType.REFUND_APPROVED,
                title,
                message,
                staffUsername,
                refund.getId(),
                "REFUND"
            );
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Refund approval notification sent successfully");
            response.put("notificationId", notification.getId());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("error", "Failed to send refund notification: " + e.getMessage()));
        }
    }
    
    /**
     * Get all notifications sent by current staff
     */
    @GetMapping("/notifications/sent")
    public ResponseEntity<List<Map<String, Object>>> getSentNotifications(Authentication authentication) {
        try {
            String staffUsername = authentication.getName();
            List<Notification> notifications = notificationService.getNotificationsBySender(staffUsername);
            
            List<Map<String, Object>> notificationData = notifications.stream().map(notif -> {
                Map<String, Object> data = new HashMap<>();
                data.put("id", notif.getId());
                data.put("type", notif.getType());
                data.put("title", notif.getTitle());
                data.put("message", notif.getMessage());
                data.put("recipientName", notif.getUser().getFirstName() + " " + notif.getUser().getLastName());
                data.put("recipientEmail", notif.getUser().getEmail());
                data.put("isRead", notif.isRead());
                data.put("createdAt", notif.getCreatedAt());
                data.put("readAt", notif.getReadAt());
                
                return data;
            }).collect(Collectors.toList());
            
            return ResponseEntity.ok(notificationData);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
    
    /**
     * Get dashboard statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            Map<String, Object> stats = new HashMap<>();
            
            // Count pending payment event bookings
            List<EventBooking> allEventBookings = eventBookingRepository.findAll();
            long pendingPayments = allEventBookings.stream()
                .filter(booking -> {
                    Optional<Payment> paymentOpt = paymentRepository.findByEventBooking(booking);
                    return paymentOpt.isEmpty() || 
                           paymentOpt.get().getPaymentStatus() == Payment.PaymentStatus.PENDING ||
                           paymentOpt.get().getPaymentStatus() == Payment.PaymentStatus.FAILED;
                })
                .filter(booking -> booking.getStatus() != EventBooking.EventBookingStatus.CANCELLED)
                .count();
            
            // Count approved refunds
            long approvedRefunds = refundRequestRepository.countByStatus(RefundRequest.RefundStatus.APPROVED);
            
            // Count total notifications sent today
            // Note: This would require a query by date, simplified here
            
            stats.put("pendingPaymentReminders", pendingPayments);
            stats.put("approvedRefunds", approvedRefunds);
            stats.put("totalEventBookings", allEventBookings.size());
            
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
