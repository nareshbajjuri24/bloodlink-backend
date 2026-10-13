package com.bloodlink.repository;

import com.bloodlink.entity.BloodRequest;
import com.bloodlink.entity.Enums.RequestStatus;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BloodRequestRepository extends JpaRepository<BloodRequest, Long> {
    List<BloodRequest> findByRequesterIdOrderByCreatedAtDesc(Long requesterId);
    long countByStatus(RequestStatus status);
}
