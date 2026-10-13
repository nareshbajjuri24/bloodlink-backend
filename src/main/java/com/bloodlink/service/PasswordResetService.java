package com.bloodlink.service;

import com.bloodlink.entity.PasswordResetToken;
import com.bloodlink.entity.User;
import com.bloodlink.exception.ApiException;
import com.bloodlink.repository.PasswordResetTokenRepository;
import com.bloodlink.repository.UserRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HexFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PasswordResetService {
    private static final SecureRandom RNG = new SecureRandom();

    private final UserRepository users;
    private final PasswordResetTokenRepository tokens;
    private final PasswordEncoder encoder;
    private final EmailService email;

    @Value("${app.frontend-url}") private String frontendUrl;
    @Value("${app.reset-token-minutes:30}") private long minutes;

    public PasswordResetService(UserRepository users, PasswordResetTokenRepository tokens,
                                PasswordEncoder encoder, EmailService email) {
        this.users = users; this.tokens = tokens; this.encoder = encoder; this.email = email;
    }

    /** Always returns normally, so the response never reveals whether an e-mail is registered. */
    @Transactional
    public void request(String emailAddress) {
        users.findByEmailIgnoreCase(emailAddress.trim()).filter(u -> u.enabled).ifPresent(u -> {
            tokens.deleteByUserId(u.id);
            byte[] bytes = new byte[32];
            RNG.nextBytes(bytes);
            String raw = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

            PasswordResetToken t = new PasswordResetToken();
            t.userId = u.id;
            t.tokenHash = hash(raw);
            t.expiresAt = Instant.now().plus(minutes, ChronoUnit.MINUTES);
            tokens.save(t);

            email.send(u.email, "[BloodLink] Reset your password",
                "Hello " + u.name + ",\n\nWe received a request to reset your BloodLink password.\n\n"
                + "Choose a new password here (valid for " + minutes + " minutes):\n"
                + frontendUrl + "/reset-password?token=" + raw
                + "\n\nIf you did not ask for this, ignore this email. Your password will not change.\n\nBloodLink");
        });
    }

    @Transactional
    public void reset(String rawToken, String newPassword) {
        ApiException invalid = new ApiException(HttpStatus.BAD_REQUEST, "This reset link is invalid or has expired");
        PasswordResetToken t = tokens.findByTokenHash(hash(rawToken)).orElseThrow(() -> invalid);
        if (t.expiresAt.isBefore(Instant.now())) throw invalid;
        User u = users.findById(t.userId).orElseThrow(() -> invalid);
        u.passwordHash = encoder.encode(newPassword);
        users.save(u);
        tokens.deleteByUserId(u.id);   // the link works only once
    }

    private static String hash(String raw) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
