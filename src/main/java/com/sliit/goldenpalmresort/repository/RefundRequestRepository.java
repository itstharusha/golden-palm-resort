package com.sliit.goldenpalmresort.repository;

import com.sliit.goldenpalmresort.model.RefundRequest;
import com.sliit.goldenpalmresort.model.RefundRequest.RefundStatus;
import com.sliit.goldenpalmresort.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RefundRequestRepository extends JpaRepository<RefundRequest, Long> {
    
    List<RefundRequest> findByStatus(RefundStatus status);
    
    List<RefundRequest> findByUser(User user);
    
    Optional<RefundRequest> findByBookingReference(String bookingReference);
    
    List<RefundRequest> findByStatusOrderByCreatedAtDesc(RefundStatus status);
    
    List<RefundRequest> findAllByOrderByCreatedAtDesc();
}
