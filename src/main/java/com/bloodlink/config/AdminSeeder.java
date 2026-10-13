package com.bloodlink.config;

import com.bloodlink.entity.User;
import com.bloodlink.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Creates the first admin (or promotes an existing account) from app.admin.email / app.admin.password. */
@Component
public class AdminSeeder implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);
    private final UserRepository users;
    private final PasswordEncoder encoder;

    @Value("${app.admin.email:}") private String adminEmail;
    @Value("${app.admin.password:}") private String adminPassword;

    public AdminSeeder(UserRepository users, PasswordEncoder encoder) { this.users = users; this.encoder = encoder; }

    @Override
    public void run(ApplicationArguments args) {
        if (adminEmail == null || adminEmail.isBlank()) return;
        String email = adminEmail.trim().toLowerCase();
        User existing = users.findByEmailIgnoreCase(email).orElse(null);
        if (existing != null) {
            if (!"ADMIN".equals(existing.role) || !existing.enabled) {
                existing.role = "ADMIN";
                existing.enabled = true;
                users.save(existing);
                log.info("Existing account {} is now an admin", email);
            }
            return;
        }
        if (adminPassword == null || adminPassword.length() < 6) {
            log.warn("app.admin.password must be at least 6 characters; admin account not created");
            return;
        }
        User u = new User();
        u.name = "Administrator";
        u.email = email;
        u.passwordHash = encoder.encode(adminPassword);
        u.phone = "0000000000";
        u.bloodGroup = "O+";
        u.city = "Admin";
        u.available = false;   // the admin never receives donor emails
        u.role = "ADMIN";
        users.save(u);
        log.info("Admin account created for {}", email);
    }
}
