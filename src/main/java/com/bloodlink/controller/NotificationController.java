package com.bloodlink.controller;

import com.bloodlink.dto.Dtos.NotificationRes;
import com.bloodlink.repository.NotificationRepository;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {
    private final NotificationRepository repo;
    public NotificationController(NotificationRepository repo) { this.repo = repo; }

    @GetMapping
    public List<NotificationRes> list(Authentication a) {
        return repo.findTop50ByUserIdOrderByCreatedAtDesc((Long) a.getPrincipal()).stream().map(NotificationRes::from).toList();
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unread(Authentication a) {
        return Map.of("count", repo.countByUserIdAndSeenFalse((Long) a.getPrincipal()));
    }

    @PostMapping("/read-all")
    public Map<String, Boolean> readAll(Authentication a) {
        repo.markAllSeen((Long) a.getPrincipal());
        return Map.of("ok", true);
    }
}
