package com.bloodlink.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "notifications")
public class Notification {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(name = "user_id", nullable = false) public Long userId;
    @Column(nullable = false, length = 160) public String title;
    @Column(nullable = false, length = 500) public String message;
    @Column(length = 200) public String link;
    @Column(name = "is_read", nullable = false) public boolean seen = false;
    @Column(name = "created_at", nullable = false) public Instant createdAt = Instant.now();
}
