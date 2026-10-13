package com.bloodlink.controller;

import com.bloodlink.dto.Dtos.*;
import com.bloodlink.entity.User;
import com.bloodlink.exception.ApiException;
import com.bloodlink.repository.UserRepository;
import com.bloodlink.service.BloodCompat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserRepository users;
    public UserController(UserRepository users) { this.users = users; }

    @GetMapping("/me")
    public UserRes me(Authentication auth) { return UserRes.from(load(auth)); }

    @PutMapping("/me")
    public UserRes update(Authentication auth, @RequestBody UpdateProfileReq q) {
        User u = load(auth);
        if (q.name() != null && !q.name().isBlank()) u.name = q.name().trim();
        if (q.phone() != null && !q.phone().isBlank()) u.phone = q.phone().trim();
        if (q.city() != null && !q.city().isBlank()) u.city = q.city().trim();
        if (q.bloodGroup() != null) {
            if (!BloodCompat.isValid(q.bloodGroup())) throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid blood group");
            u.bloodGroup = q.bloodGroup();
        }
        if (q.address() != null) u.address = q.address().isBlank() ? null : q.address().trim();
        if (q.available() != null) u.available = q.available();
        u.lastDonationDate = q.lastDonationDate();
        return UserRes.from(users.save(u));
    }

    private User load(Authentication auth) {
        return users.findById((Long) auth.getPrincipal())
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Please log in again"));
    }
}
