package com.sliit.goldenpalmresort.controller;

import com.sliit.goldenpalmresort.model.Booking;
import com.sliit.goldenpalmresort.model.EventBooking;
import com.sliit.goldenpalmresort.model.Payment;
import com.sliit.goldenpalmresort.repository.BookingRepository;
import com.sliit.goldenpalmresort.repository.EventBookingRepository;
import com.sliit.goldenpalmresort.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private EventBookingRepository eventBookingRepository;

    @PostMapping("/room")
    public ResponseEntity<?> createRoomPayment(@RequestBody Map<String, Object> request, Authentication authentication) {
        try {
            String bookingReference = String.valueOf(request.get("bookingReference"));
            String methodStr = String.valueOf(request.get("paymentMethod"));
            BigDecimal amount = new BigDecimal(String.valueOf(request.get("amount")));

            Optional<Booking> bookingOpt = bookingRepository.findByBookingReference(bookingReference);
            if (bookingOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Booking not found"));
            }
            Booking booking = bookingOpt.get();

            if (amount.compareTo(booking.getTotalAmount()) != 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid amount"));
            }

            Payment payment = new Payment();
            payment.setBooking(booking);
            payment.setAmount(amount);
            payment.setPaymentMethod(Payment.PaymentMethod.valueOf(methodStr.toUpperCase()));
            payment.setPaymentStatus(Payment.PaymentStatus.PENDING);
            payment.setNotes("Initiated by " + (authentication != null ? authentication.getName() : "anonymous"));

            Payment saved = paymentRepository.save(payment);
            Map<String, Object> resp = new HashMap<>();
            resp.put("paymentId", saved.getId());
            resp.put("status", saved.getPaymentStatus().name());
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/event")
    public ResponseEntity<?> createEventPayment(@RequestBody Map<String, Object> request, Authentication authentication) {
        try {
            String bookingReference = String.valueOf(request.get("bookingReference"));
            String methodStr = String.valueOf(request.get("paymentMethod"));
            BigDecimal amount = new BigDecimal(String.valueOf(request.get("amount")));

            Optional<EventBooking> eventOpt = eventBookingRepository.findByBookingReference(bookingReference);
            if (eventOpt.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Event booking not found"));
            }
            EventBooking eventBooking = eventOpt.get();

            if (eventBooking.getTotalAmount() != null && amount.compareTo(eventBooking.getTotalAmount()) != 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid amount"));
            }

            Payment payment = new Payment();
            payment.setEventBooking(eventBooking);
            payment.setAmount(amount);
            payment.setPaymentMethod(Payment.PaymentMethod.valueOf(methodStr.toUpperCase()));
            payment.setPaymentStatus(Payment.PaymentStatus.PENDING);
            payment.setNotes("Initiated by " + (authentication != null ? authentication.getName() : "anonymous"));

            Payment saved = paymentRepository.save(payment);
            Map<String, Object> resp = new HashMap<>();
            resp.put("paymentId", saved.getId());
            resp.put("status", saved.getPaymentStatus().name());
            return ResponseEntity.status(HttpStatus.CREATED).body(resp);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));
        }
    }
}
