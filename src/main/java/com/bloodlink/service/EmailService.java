package com.bloodlink.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Sends e-mail through Gmail SMTP (app.mail.provider=smtp) or the Brevo HTTP API (app.mail.provider=brevo).
 * Use brevo on hosts that block outbound SMTP ports, such as Render's free tier.
 */
@Service
public class EmailService {
    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender sender;
    private final HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${spring.mail.username:}") private String username;
    @Value("${app.mail.from}") private String from;
    @Value("${app.mail.provider:smtp}") private String provider;
    @Value("${app.brevo.api-key:}") private String brevoKey;

    public EmailService(JavaMailSender sender) { this.sender = sender; }

    @Async
    public void send(String to, String subject, String text) {
        if ("brevo".equalsIgnoreCase(provider)) sendBrevo(to, subject, text);
        else sendSmtp(to, subject, text);
    }

    private void sendSmtp(String to, String subject, String text) {
        if (username == null || username.isBlank()) {
            log.warn("Mail is not configured (spring.mail.username empty). Would have sent '{}' to {}", subject, to);
            return;
        }
        try {
            SimpleMailMessage m = new SimpleMailMessage();
            m.setFrom(from);
            m.setTo(to);
            m.setSubject(subject);
            m.setText(text);
            sender.send(m);
            log.info("Email sent to {}", to);
        } catch (Exception e) {
            log.error("Could not send email to {}: {}", to, e.getMessage());
        }
    }

    private void sendBrevo(String to, String subject, String text) {
        if (brevoKey == null || brevoKey.isBlank()) {
            log.warn("BREVO_API_KEY is empty. Would have sent '{}' to {}", subject, to);
            return;
        }
        try {
            String json = mapper.writeValueAsString(Map.of(
                "sender", Map.of("name", "BloodLink", "email", from),
                "to", List.of(Map.of("email", to)),
                "subject", subject,
                "textContent", text));
            HttpRequest req = HttpRequest.newBuilder(URI.create("https://api.brevo.com/v3/smtp/email"))
                .timeout(Duration.ofSeconds(15))
                .header("api-key", brevoKey)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();
            HttpResponse<String> res = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() / 100 == 2) log.info("Email sent to {} via Brevo", to);
            else log.error("Brevo rejected email to {}: HTTP {} {}", to, res.statusCode(), res.body());
        } catch (Exception e) {
            log.error("Could not send email to {} via Brevo: {}", to, e.getMessage());
        }
    }
}
