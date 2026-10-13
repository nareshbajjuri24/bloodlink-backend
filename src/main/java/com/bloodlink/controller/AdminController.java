package com.bloodlink.controller;

import com.bloodlink.dto.Dtos.*;
import com.bloodlink.service.AdminService;
import com.bloodlink.service.RequestService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** Every route here requires ROLE_ADMIN (see SecurityConfig). */
@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService admin;
    private final RequestService requests;

    public AdminController(AdminService admin, RequestService requests) { this.admin = admin; this.requests = requests; }

    private Long uid(Authentication a) { return (Long) a.getPrincipal(); }

    @GetMapping("/stats")
    public StatsRes stats() { return admin.stats(); }

    @GetMapping("/users")
    public List<AdminUserRes> users() { return admin.listUsers(); }

    @PutMapping("/users/{id}/enabled")
    public AdminUserRes enabled(Authentication a, @PathVariable Long id, @RequestBody SetEnabledReq q) {
        return admin.setEnabled(uid(a), id, q.enabled());
    }

    @PutMapping("/users/{id}/role")
    public AdminUserRes role(Authentication a, @PathVariable Long id, @Valid @RequestBody SetRoleReq q) {
        return admin.setRole(uid(a), id, q.role());
    }

    @DeleteMapping("/users/{id}")
    public Map<String, Boolean> delete(Authentication a, @PathVariable Long id) {
        admin.deleteUser(uid(a), id);
        return Map.of("ok", true);
    }

    @GetMapping("/requests")
    public List<RequestRes> allRequests() { return requests.adminAll(); }

    @GetMapping("/requests/{id}")
    public RequestRes request(@PathVariable Long id) { return requests.adminDetail(id); }

    @PostMapping("/requests/{id}/close")
    public RequestRes close(@PathVariable Long id) { return requests.adminClose(id); }

    @DeleteMapping("/requests/{id}")
    public Map<String, Boolean> deleteRequest(@PathVariable Long id) {
        requests.adminDelete(id);
        return Map.of("ok", true);
    }
}
