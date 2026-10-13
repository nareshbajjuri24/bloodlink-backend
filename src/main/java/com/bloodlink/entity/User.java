package com.bloodlink.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;
    @Column(nullable = false, length = 120) public String name;
    @Column(nullable = false, unique = true, length = 160) public String email;
    @Column(name = "password_hash", nullable = false, length = 100) public String passwordHash;
    @Column(nullable = false, length = 30) public String phone;
    @Column(name = "blood_group", nullable = false, length = 5) public String bloodGroup;
    @Column(nullable = false, length = 80) public String city;
    @Column(length = 255) public String address;
    @Column(nullable = false) public boolean available = true;
    @Column(name = "last_donation_date") public LocalDate lastDonationDate;
    @Column(nullable = false, length = 20, columnDefinition = "varchar(20) default 'USER'") public String role = "USER";
    @Column(nullable = false, columnDefinition = "boolean default true") public boolean enabled = true;
    @Column(name = "created_at", nullable = false) public Instant createdAt = Instant.now();
}
