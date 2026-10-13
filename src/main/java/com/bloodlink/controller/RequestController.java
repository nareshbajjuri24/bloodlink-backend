package com.bloodlink.controller;

import com.bloodlink.dto.Dtos.*;
import com.bloodlink.service.RequestService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/requests")
public class RequestController {
    private final RequestService service;
    public RequestController(RequestService service) { this.service = service; }

    private Long uid(Authentication a) { return (Long) a.getPrincipal(); }

    @PostMapping
    public RequestRes create(Authentication a, @Valid @RequestBody CreateRequestReq q) { return service.create(uid(a), q); }

    @GetMapping("/mine")
    public List<RequestRes> mine(Authentication a) { return service.mine(uid(a)); }

    @GetMapping("/incoming")
    public List<RequestRes> incoming(Authentication a) { return service.incoming(uid(a)); }

    @GetMapping("/{id}")
    public RequestRes detail(Authentication a, @PathVariable Long id) { return service.detail(uid(a), id); }

    @PostMapping("/{id}/accept")
    public RequestRes accept(Authentication a, @PathVariable Long id) { return service.respond(uid(a), id, true); }

    @PostMapping("/{id}/decline")
    public RequestRes decline(Authentication a, @PathVariable Long id) { return service.respond(uid(a), id, false); }

    @PostMapping("/{id}/close")
    public RequestRes close(Authentication a, @PathVariable Long id) { return service.close(uid(a), id); }
}
