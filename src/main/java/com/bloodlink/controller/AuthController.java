package com.bloodlink.controller;

import com.bloodlink.config.JwtService;
import com.bloodlink.dto.Dtos.*;
import com.bloodlink.entity.User;
import com.bloodlink.exception.ApiException;
import com.bloodlink.repository.UserRepository;
import com.bloodlink.service.BloodCompat;
import com.bloodlink.service.PasswordResetService;
import jakarta.validation.Valid;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final JwtService jwt;
    private final PasswordResetService resets;

    public AuthController(UserRepository users, PasswordEncoder encoder, JwtService jwt, PasswordResetService resets) {
        this.users = users; this.encoder = encoder; this.jwt = jwt; this.resets = resets;
    }

    @PostMapping("/register")
    public AuthRes register(@Valid @RequestBody RegisterReq q) {
        if (!BloodCompat.isValid(q.bloodGroup())) throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid blood group");
        if (users.existsByEmailIgnoreCase(q.email().trim())) throw new ApiException(HttpStatus.CONFLICT, "That email is already registered");
        User u = new User();
        u.name = q.name().trim();
        u.email = q.email().trim().toLowerCase();
        u.passwordHash = encoder.encode(q.password());
        u.phone = q.phone().trim();
        u.bloodGroup = q.bloodGroup();
        u.city = q.city().trim();
        u.address = q.address() == null || q.address().isBlank() ? null : q.address().trim();
        u = users.save(u);
        return new AuthRes(jwt.create(u.id), UserRes.from(u));
    }

    @PostMapping("/login")
    public AuthRes login(@Valid @RequestBody LoginReq q) {
        User u = users.findByEmailIgnoreCase(q.email().trim())
            .filter(x -> encoder.matches(q.password(), x.passwordHash))
            .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!u.enabled) throw new ApiException(HttpStatus.FORBIDDEN, "Your account has been blocked. Contact the administrator.");
        return new AuthRes(jwt.create(u.id), UserRes.from(u));
    }

    @PostMapping("/forgot-password")
    public Map<String, String> forgot(@Valid @RequestBody ForgotReq q) {
        resets.request(q.email());
        return Map.of("message", "If that email is registered, we have sent a link to reset the password.");
    }

    @PostMapping("/reset-password")
    public Map<String, String> reset(@Valid @RequestBody ResetReq q) {
        resets.reset(q.token(), q.newPassword());
        return Map.of("message", "Password updated. You can log in now.");
    }
}
