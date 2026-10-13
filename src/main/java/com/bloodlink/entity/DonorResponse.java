package com.bloodlink.entity;

import com.bloodlink.entity.Enums.ResponseStatus;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "donor_responses", uniqueConstraints = @UniqueConstraint(columnNames = {"request_id", "donor_id"}))
public class DonorResponse {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "request_id", nullable = false) public Long requestId;
    @Column(name = "donor_id", nullable = false) public Long donorId;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) public ResponseStatus status = ResponseStatus.NOTIFIED;
    @Column(name = "notified_at", nullable = false) public Instant notifiedAt = Instant.now();
    @Column(name = "responded_at") public Instant respondedAt;
}
