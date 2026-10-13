package com.bloodlink.dto;

import com.bloodlink.entity.Enums.Urgency;
import com.bloodlink.entity.Notification;
import com.bloodlink.entity.User;
import jakarta.validation.constraints.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class Dtos {
    private Dtos() {}

    public record RegisterReq(
        @NotBlank(message = "Name is required") String name,
        @NotBlank(message = "Email is required") @Email(message = "Enter a valid email") String email,
        @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String password,
        @NotBlank(message = "Phone is required") String phone,
        @NotBlank(message = "Blood group is required") String bloodGroup,
        @NotBlank(message = "City is required") String city,
        String address) {}

    public record LoginReq(@NotBlank String email, @NotBlank String password) {}

    public record UpdateProfileReq(String name, String phone, String bloodGroup, String city, String address,
                                   Boolean available, LocalDate lastDonationDate) {}

    public record UserRes(Long id, String name, String email, String phone, String bloodGroup, String city,
                          String address, boolean available, LocalDate lastDonationDate, String role) {
        public static UserRes from(User u) {
            return new UserRes(u.id, u.name, u.email, u.phone, u.bloodGroup, u.city, u.address, u.available, u.lastDonationDate, u.role);
        }
    }

    public record AuthRes(String token, UserRes user) {}

    public record CreateRequestReq(
        @NotBlank(message = "Patient name is required") String patientName,
        @NotBlank(message = "Blood group is required") String bloodGroup,
        @Min(value = 1, message = "Units must be 1 to 10") @Max(value = 10, message = "Units must be 1 to 10") int units,
        @NotBlank(message = "Hospital is required") String hospital,
        @NotBlank(message = "City is required") String city,
        @Size(max = 500, message = "Message is too long") String message,
        Urgency urgency) {}

    /** Only ever sent to the requester, and only for donors who accepted. */
    public record DonorContact(Long donorId, String name, String phone, String email, String bloodGroup,
                               String city, String address, Instant respondedAt) {}

    /** Only ever sent to a donor who accepted the request. */
    public record RequesterContact(String name, String phone, String email) {}

    public record RequestRes(Long id, String patientName, String bloodGroup, int units, String hospital, String city,
                             String message, String urgency, String status, Instant createdAt, String requesterName,
                             int notifiedCount, int acceptedCount, String myStatus,
                             List<DonorContact> acceptedDonors, RequesterContact requesterContact) {}

    public record NotificationRes(Long id, String title, String message, String link, boolean seen, Instant createdAt) {
        public static NotificationRes from(Notification n) {
            return new NotificationRes(n.id, n.title, n.message, n.link, n.seen, n.createdAt);
        }
    }

    public record ForgotReq(@NotBlank(message = "Email is required") @Email(message = "Enter a valid email") String email) {}

    public record ResetReq(@NotBlank(message = "Reset token is missing") String token,
                           @NotBlank @Size(min = 6, message = "Password must be at least 6 characters") String newPassword) {}

    public record SetEnabledReq(boolean enabled) {}
    public record SetRoleReq(@NotBlank String role) {}

    public record AdminUserRes(Long id, String name, String email, String phone, String bloodGroup, String city,
                               boolean available, String role, boolean enabled, Instant createdAt) {
        public static AdminUserRes from(User u) {
            return new AdminUserRes(u.id, u.name, u.email, u.phone, u.bloodGroup, u.city, u.available, u.role, u.enabled, u.createdAt);
        }
    }

    public record StatsRes(long users, long availableDonors, long requests, long openRequests,
                           long closedRequests, long acceptedResponses) {}
}
