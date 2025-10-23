package com.sliit.goldenpalmresort.service;

import com.sliit.goldenpalmresort.dto.RefundRequestDTO;
import com.sliit.goldenpalmresort.dto.RefundRequestResponse;
import com.sliit.goldenpalmresort.model.*;
import com.sliit.goldenpalmresort.model.Booking.BookingStatus;
import com.sliit.goldenpalmresort.model.Payment.PaymentStatus;
import com.sliit.goldenpalmresort.model.RefundRequest.RefundStatus;
import com.sliit.goldenpalmresort.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class RefundRequestService {
    
    @Autowired
    private RefundRequestRepository refundRequestRepository;
    
    @Autowired
    private BookingRepository bookingRepository;
    
    @Autowired
    private EventBookingRepository eventBookingRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    @Transactional
    public RefundRequestResponse createRefundRequest(RefundRequestDTO dto, String username) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setUser(user);
        refundRequest.setBookingReference(dto.getBookingReference());
        refundRequest.setRefundAmount(dto.getRefundAmount());
        refundRequest.setBankAccountNumber(dto.getBankAccountNumber());
        refundRequest.setBankName(dto.getBankName());
        refundRequest.setBankBranch(dto.getBankBranch());
        refundRequest.setAccountHolderName(dto.getAccountHolderName());
        refundRequest.setReason(dto.getReason());
        refundRequest.setStatus(RefundStatus.PENDING);
        
        // Link to booking
        if ("ROOM".equals(dto.getBookingType())) {
            Booking booking = bookingRepository.findByBookingReference(dto.getBookingReference())
                .orElseThrow(() -> new RuntimeException("Booking not found"));
            refundRequest.setBooking(booking);
        } else if ("EVENT".equals(dto.getBookingType())) {
            EventBooking eventBooking = eventBookingRepository.findByBookingReference(dto.getBookingReference())
                .orElseThrow(() -> new RuntimeException("Event booking not found"));
            refundRequest.setEventBooking(eventBooking);
        }
        
        RefundRequest saved = refundRequestRepository.save(refundRequest);
        return mapToResponse(saved);
    }
    
    public List<RefundRequestResponse> getAllRefundRequests() {
        List<RefundRequest> requests = refundRequestRepository.findAllByOrderByCreatedAtDesc();
        List<RefundRequestResponse> responses = new ArrayList<>();
        for (RefundRequest request : requests) {
            responses.add(mapToResponse(request));
        }
        return responses;
    }
    
    public List<RefundRequestResponse> getPendingRefundRequests() {
        List<RefundRequest> requests = refundRequestRepository.findByStatusOrderByCreatedAtDesc(RefundStatus.PENDING);
        List<RefundRequestResponse> responses = new ArrayList<>();
        for (RefundRequest request : requests) {
            responses.add(mapToResponse(request));
        }
        return responses;
    }
    
    public RefundRequestResponse getRefundRequestById(Long id) {
        RefundRequest request = refundRequestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Refund request not found"));
        return mapToResponse(request);
    }
    
    @Transactional
    public RefundRequestResponse approveRefundRequest(Long id, String processedBy) {
        RefundRequest request = refundRequestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Refund request not found"));
        
        if (request.getStatus() != RefundStatus.PENDING) {
            throw new RuntimeException("Refund request is not pending");
        }
        
        // Update refund request status
        request.setStatus(RefundStatus.APPROVED);
        request.setProcessedBy(processedBy);
        request.setProcessedAt(LocalDateTime.now());
        
        // Update booking status to CANCELLED
        if (request.getBooking() != null) {
            Booking booking = request.getBooking();
            booking.setStatus(BookingStatus.CANCELLED);
            bookingRepository.save(booking);
            
            // Update payment status to REFUNDED
            Payment payment = paymentRepository.findByBooking(booking).orElse(null);
            if (payment != null) {
                payment.setPaymentStatus(PaymentStatus.REFUNDED);
                payment.setRefundAmount(request.getRefundAmount());
                payment.setRefundReason(request.getReason());
                payment.setRefundDate(LocalDateTime.now());
                paymentRepository.save(payment);
            }
        } else if (request.getEventBooking() != null) {
            EventBooking eventBooking = request.getEventBooking();
            eventBooking.setStatus(EventBooking.EventBookingStatus.CANCELLED);
            eventBookingRepository.save(eventBooking);
            
            // Update payment status to REFUNDED
            Payment payment = paymentRepository.findByEventBooking(eventBooking).orElse(null);
            if (payment != null) {
                payment.setPaymentStatus(PaymentStatus.REFUNDED);
                payment.setRefundAmount(request.getRefundAmount());
                payment.setRefundReason(request.getReason());
                payment.setRefundDate(LocalDateTime.now());
                paymentRepository.save(payment);
            }
        }
        
        RefundRequest saved = refundRequestRepository.save(request);
        return mapToResponse(saved);
    }
    
    @Transactional
    public RefundRequestResponse rejectRefundRequest(Long id, String processedBy, String notes) {
        RefundRequest request = refundRequestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Refund request not found"));
        
        if (request.getStatus() != RefundStatus.PENDING) {
            throw new RuntimeException("Refund request is not pending");
        }
        
        request.setStatus(RefundStatus.REJECTED);
        request.setProcessedBy(processedBy);
        request.setProcessedAt(LocalDateTime.now());
        request.setNotes(notes);
        
        RefundRequest saved = refundRequestRepository.save(request);
        return mapToResponse(saved);
    }
    
    private RefundRequestResponse mapToResponse(RefundRequest request) {
        RefundRequestResponse response = new RefundRequestResponse();
        response.setId(request.getId());
        response.setBookingReference(request.getBookingReference());
        response.setUserName(request.getUser().getFirstName() + " " + request.getUser().getLastName());
        response.setUserEmail(request.getUser().getEmail());
        response.setRefundAmount(request.getRefundAmount());
        response.setBankAccountNumber(request.getBankAccountNumber());
        response.setBankName(request.getBankName());
        response.setBankBranch(request.getBankBranch());
        response.setAccountHolderName(request.getAccountHolderName());
        response.setStatus(request.getStatus().toString());
        response.setReason(request.getReason());
        response.setNotes(request.getNotes());
        response.setProcessedBy(request.getProcessedBy());
        response.setProcessedAt(request.getProcessedAt());
        response.setCreatedAt(request.getCreatedAt());
        
        // Set booking details
        if (request.getBooking() != null) {
            response.setBookingType("ROOM");
            Booking booking = request.getBooking();
            response.setRoomNumber(booking.getRoom().getRoomNumber());
            response.setRoomType(booking.getRoom().getRoomType().toString());
            response.setCheckInDate(booking.getCheckInDate().toString());
            response.setCheckOutDate(booking.getCheckOutDate().toString());
        } else if (request.getEventBooking() != null) {
            response.setBookingType("EVENT");
            EventBooking eventBooking = request.getEventBooking();
            response.setEventSpaceName(eventBooking.getEventSpace().getName());
            response.setEventDate(eventBooking.getEventDate().toString());
            response.setEventType(eventBooking.getEventType());
        }
        
        return response;
    }
}
