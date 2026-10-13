package com.bloodlink.service;

import com.bloodlink.dto.Dtos.AdminUserRes;
import com.bloodlink.dto.Dtos.StatsRes;
import com.bloodlink.entity.Enums.RequestStatus;
import com.bloodlink.entity.Enums.ResponseStatus;
import com.bloodlink.entity.User;
import com.bloodlink.exception.ApiException;
import com.bloodlink.repository.*;
import java.util.List;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {
    private final UserRepository users;
    private final BloodRequestRepository requests;
    private final DonorResponseRepository responses;
    private final NotificationRepository notifications;
    private final PasswordResetTokenRepository tokens;

    public AdminService(UserRepository users, BloodRequestRepository requests, DonorResponseRepository responses,
                        NotificationRepository notifications, PasswordResetTokenRepository tokens) {
        this.users = users; this.requests = requests; this.responses = responses;
        this.notifications = notifications; this.tokens = tokens;
    }

    @Transactional(readOnly = true)
    public StatsRes stats() {
        return new StatsRes(users.count(), users.countByAvailableTrue(), requests.count(),
            requests.countByStatus(RequestStatus.OPEN), requests.countByStatus(RequestStatus.CLOSED),
            responses.countByStatus(ResponseStatus.ACCEPTED));
    }

    @Transactional(readOnly = true)
    public List<AdminUserRes> listUsers() {
        return users.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream().map(AdminUserRes::from).toList();
    }

    @Transactional
    public AdminUserRes setEnabled(Long adminId, Long id, boolean enabled) {
        if (adminId.equals(id)) throw new ApiException(HttpStatus.BAD_REQUEST, "You cannot block your own account");
        User u = find(id);
        u.enabled = enabled;
        return AdminUserRes.from(users.save(u));
    }

    @Transactional
    public AdminUserRes setRole(Long adminId, Long id, String role) {
        if (!"USER".equals(role) && !"ADMIN".equals(role)) throw new ApiException(HttpStatus.BAD_REQUEST, "Role must be USER or ADMIN");
        if (adminId.equals(id)) throw new ApiException(HttpStatus.BAD_REQUEST, "You cannot change your own role");
        User u = find(id);
        u.role = role;
        return AdminUserRes.from(users.save(u));
    }

    /** Removes the user together with their requests, donor responses, notifications and reset tokens. */
    @Transactional
    public void deleteUser(Long adminId, Long id) {
        if (adminId.equals(id)) throw new ApiException(HttpStatus.BAD_REQUEST, "You cannot delete your own account");
        User u = find(id);
        List<Long> requestIds = requests.findByRequesterIdOrderByCreatedAtDesc(id).stream().map(r -> r.id).toList();
        if (!requestIds.isEmpty()) {
            responses.deleteByRequestIdIn(requestIds);
            requests.deleteAllById(requestIds);
        }
        responses.deleteByDonorId(id);
        notifications.deleteByUserId(id);
        tokens.deleteByUserId(id);
        users.delete(u);
    }

    private User find(Long id) {
        return users.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
