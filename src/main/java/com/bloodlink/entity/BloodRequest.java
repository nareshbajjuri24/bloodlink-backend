package com.bloodlink.entity;

import com.bloodlink.entity.Enums.RequestStatus;
import com.bloodlink.entity.Enums.Urgency;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "blood_requests")
public class BloodRequest {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "requester_id", nullable = false) public Long requesterId;
    @Column(name = "patient_name", nullable = false, length = 120) public String patientName;
    @Column(name = "blood_group", nullable = false, length = 5) public String bloodGroup;
    @Column(nullable = false) public int units;
    @Column(nullable = false, length = 160) public String hospital;
    @Column(nullable = false, length = 80) public String city;
    @Column(length = 500) public String message;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) public Urgency urgency = Urgency.NORMAL;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 20) public RequestStatus status = RequestStatus.OPEN;
    @Column(name = "created_at", nullable = false) public Instant createdAt = Instant.now();
}
