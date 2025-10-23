package com.sliit.goldenpalmresort.controller;

import com.sliit.goldenpalmresort.dto.RefundRequestDTO;
import com.sliit.goldenpalmresort.dto.RefundRequestResponse;
import com.sliit.goldenpalmresort.service.RefundRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/refund-requests")
@CrossOrigin(origins = "*")
public class RefundRequestController {
    
    @Autowired
    private RefundRequestService refundRequestService;
    
    @PostMapping
    public ResponseEntity<RefundRequestResponse> createRefundRequest(
            @RequestBody RefundRequestDTO request, 
            Authentication authentication) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            String username = authentication.getName();
            RefundRequestResponse response = refundRequestService.createRefundRequest(request, username);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'PAYMENT_OFFICER', 'MANAGER')")
    public ResponseEntity<List<RefundRequestResponse>> getAllRefundRequests() {
        try {
            List<RefundRequestResponse> requests = refundRequestService.getAllRefundRequests();
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/pending")
    @PreAuthorize("hasAnyRole('ADMIN', 'PAYMENT_OFFICER', 'MANAGER')")
    public ResponseEntity<List<RefundRequestResponse>> getPendingRefundRequests() {
        try {
            List<RefundRequestResponse> requests = refundRequestService.getPendingRefundRequests();
            return ResponseEntity.ok(requests);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PAYMENT_OFFICER', 'MANAGER')")
    public ResponseEntity<RefundRequestResponse> getRefundRequestById(@PathVariable Long id) {
        try {
            RefundRequestResponse request = refundRequestService.getRefundRequestById(id);
            return ResponseEntity.ok(request);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN', 'PAYMENT_OFFICER')")
    public ResponseEntity<RefundRequestResponse> approveRefundRequest(
            @PathVariable Long id, 
            Authentication authentication) {
        try {
            String processedBy = authentication.getName();
            RefundRequestResponse response = refundRequestService.approveRefundRequest(id, processedBy);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
    
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAnyRole('ADMIN', 'PAYMENT_OFFICER')")
    public ResponseEntity<RefundRequestResponse> rejectRefundRequest(
            @PathVariable Long id,
            @RequestBody Map<String, String> body,
            Authentication authentication) {
        try {
            String processedBy = authentication.getName();
            String notes = body.get("notes");
            RefundRequestResponse response = refundRequestService.rejectRefundRequest(id, processedBy, notes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }
}
