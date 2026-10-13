package com.bloodlink.repository;

import com.bloodlink.entity.DonorResponse;
import com.bloodlink.entity.Enums.ResponseStatus;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DonorResponseRepository extends JpaRepository<DonorResponse, Long> {
    List<DonorResponse> findByRequestId(Long requestId);
    List<DonorResponse> findByDonorIdOrderByNotifiedAtDesc(Long donorId);
    Optional<DonorResponse> findByRequestIdAndDonorId(Long requestId, Long donorId);
    long countByStatus(ResponseStatus status);
    void deleteByRequestId(Long requestId);
    void deleteByRequestIdIn(Collection<Long> requestIds);
    void deleteByDonorId(Long donorId);
}
