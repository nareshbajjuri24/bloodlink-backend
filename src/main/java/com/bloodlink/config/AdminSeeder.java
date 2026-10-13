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

@Component
public class AdminSeeder implements ApplicationRunner {

    private static final Logger log =
            LoggerFactory.getLogger(AdminSeeder.class);

    private final UserRepository users;
    private final PasswordEncoder encoder;

    @Value("${app.admin.email:}")
    private String adminEmail;

    @Value("${app.admin.password:}")
    private String adminPassword;

    public AdminSeeder(UserRepository users,
                       PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @Override
    public void run(ApplicationArguments args) {

        log.info("AdminSeeder started. Admin email configured: {}",
                adminEmail != null && !adminEmail.isBlank());

        if (adminEmail == null || adminEmail.isBlank()) {
            log.warn("ADMIN_EMAIL is empty. Admin setup skipped.");
            return;
        }

        String email = adminEmail.trim().toLowerCase();

        User existing = users.findByEmailIgnoreCase(email).orElse(null);

        if (existing != null) {

            existing.role = "ADMIN";
            existing.enabled = true;

            if (adminPassword != null && !adminPassword.isBlank()) {
                existing.passwordHash = encoder.encode(adminPassword);
            } else {
                log.warn("ADMIN_PASSWORD is empty. Existing password retained.");
            }

            users.save(existing);

            log.info("Existing account promoted to ADMIN and saved.");
            return;
        }

        if (adminPassword == null || adminPassword.length() < 6) {
            log.warn("ADMIN_PASSWORD must be at least 6 characters. Admin account not created.");
            return;
        }

        User admin = new User();
        admin.name = "Administrator";
        admin.email = email;
        admin.passwordHash = encoder.encode(adminPassword);
        admin.phone = "0000000000";
        admin.bloodGroup = "O+";
        admin.city = "Admin";
        admin.available = false;
        admin.role = "ADMIN";
        admin.enabled = true;

        users.save(admin);

        log.info("Admin account created successfully.");
    }
}