package com.bloodlink.service;

import com.bloodlink.dto.Dtos.*;
import com.bloodlink.entity.*;
import com.bloodlink.entity.Enums.*;
import com.bloodlink.exception.ApiException;
import com.bloodlink.repository.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RequestService {
    private final BloodRequestRepository requests;
    private final DonorResponseRepository responses;
    private final UserRepository users;
    private final NotificationRepository notifications;
    private final EmailService email;

    @Value("${app.frontend-url}") private String frontendUrl;
    @Value("${app.donor-gap-days:90}") private int gapDays;

    public RequestService(BloodRequestRepository requests, DonorResponseRepository responses, UserRepository users,
                          NotificationRepository notifications, EmailService email) {
        this.requests = requests; this.responses = responses; this.users = users;
        this.notifications = notifications; this.email = email;
    }

    // ---------- create + notify matching donors ----------
    @Transactional
    public RequestRes create(Long userId, CreateRequestReq q) {
        if (!BloodCompat.isValid(q.bloodGroup())) throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid blood group");
        User me = user(userId);

        BloodRequest r = new BloodRequest();
        r.requesterId = userId;
        r.patientName = q.patientName().trim();
        r.bloodGroup = q.bloodGroup();
        r.units = q.units();
        r.hospital = q.hospital().trim();
        r.city = q.city().trim();
        r.message = q.message() == null || q.message().isBlank() ? null : q.message().trim();
        r.urgency = q.urgency() == null ? Urgency.NORMAL : q.urgency();
        r = requests.save(r);

        List<User> donors = users.findMatchingDonors(BloodCompat.donorsFor(r.bloodGroup), r.city, userId,
            LocalDate.now().minusDays(gapDays));

        for (User d : donors) {
            DonorResponse dr = new DonorResponse();
            dr.requestId = r.id;
            dr.donorId = d.id;
            responses.save(dr);

            String title = r.urgency + ": " + r.bloodGroup + " blood needed in " + r.city;
            String body = r.patientName + " needs " + r.units + " unit(s) of " + r.bloodGroup + " at " + r.hospital + ", " + r.city + ".";
            notify(d.id, title, body, "/requests/" + r.id);

            email.send(d.email, "[BloodLink] " + title,
                "Hello " + d.name + ",\n\n" + body
                + (r.message != null ? "\nNote from requester: " + r.message : "")
                + "\n\nYou are receiving this because your blood group (" + d.bloodGroup + ") matches and you are available to donate in " + d.city + "."
                + "\n\nOpen the request and accept or decline:\n" + frontendUrl + "/requests/" + r.id
                + "\n\nThank you for being a donor.\nBloodLink");
        }
        return toRes(r, me, donors.size(), 0, null, List.of(), null);
    }

    // ---------- lists ----------
    @Transactional(readOnly = true)
    public List<RequestRes> mine(Long userId) {
        User me = user(userId);
        return requests.findByRequesterIdOrderByCreatedAtDesc(userId).stream().map(r -> {
            List<DonorResponse> rs = responses.findByRequestId(r.id);
            return toRes(r, me, rs.size(), count(rs, ResponseStatus.ACCEPTED), null, List.of(), null);
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<RequestRes> incoming(Long donorId) {
        return responses.findByDonorIdOrderByNotifiedAtDesc(donorId).stream().map(dr -> {
            BloodRequest r = requests.findById(dr.requestId).orElseThrow();
            return toRes(r, user(r.requesterId), 0, 0, dr.status.name(), List.of(), null);
        }).toList();
    }

    // ---------- detail ----------
    @Transactional(readOnly = true)
    public RequestRes detail(Long viewerId, Long requestId) {
        BloodRequest r = requests.findById(requestId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Request not found"));
        User requester = user(r.requesterId);

        if (r.requesterId.equals(viewerId)) {
            List<DonorResponse> rs = responses.findByRequestId(r.id);
            List<DonorContact> accepted = rs.stream().filter(x -> x.status == ResponseStatus.ACCEPTED).map(x -> {
                User d = user(x.donorId);
                return new DonorContact(d.id, d.name, d.phone, d.email, d.bloodGroup, d.city, d.address, x.respondedAt);
            }).toList();
            return toRes(r, requester, rs.size(), accepted.size(), null, accepted, null);
        }

        DonorResponse mine = responses.findByRequestIdAndDonorId(requestId, viewerId)
            .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "This request was not sent to you"));
        RequesterContact contact = mine.status == ResponseStatus.ACCEPTED
            ? new RequesterContact(requester.name, requester.phone, requester.email) : null;
        return toRes(r, requester, 0, 0, mine.status.name(), List.of(), contact);
    }

    // ---------- donor accepts / declines ----------
    @Transactional
    public RequestRes respond(Long donorId, Long requestId, boolean accept) {
        BloodRequest r = requests.findById(requestId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Request not found"));
        DonorResponse dr = responses.findByRequestIdAndDonorId(requestId, donorId)
            .orElseThrow(() -> new ApiException(HttpStatus.FORBIDDEN, "This request was not sent to you"));
        if (r.status != RequestStatus.OPEN) throw new ApiException(HttpStatus.CONFLICT, "This request is already closed");

        boolean alreadyAccepted = dr.status == ResponseStatus.ACCEPTED;
        dr.status = accept ? ResponseStatus.ACCEPTED : ResponseStatus.DECLINED;
        dr.respondedAt = Instant.now();
        responses.save(dr);

        if (accept && !alreadyAccepted) {
            User donor = user(donorId);
            User requester = user(r.requesterId);
            String title = donor.name + " accepted your " + r.bloodGroup + " request";
            notify(requester.id, title, "Open the request to see contact details and location.", "/requests/" + r.id);
            email.send(requester.email, "[BloodLink] " + title,
                "Hello " + requester.name + ",\n\nGood news! A donor accepted your request for " + r.bloodGroup
                + " blood at " + r.hospital + ".\n\nDonor details\n"
                + "Name: " + donor.name + "\nPhone: " + donor.phone + "\nEmail: " + donor.email
                + "\nCity: " + donor.city + (donor.address != null ? "\nAddress: " + donor.address : "")
                + "\n\nView the request: " + frontendUrl + "/requests/" + r.id + "\n\nBloodLink");
        }
        return detail(donorId, requestId);
    }

    // ---------- requester closes ----------
    @Transactional
    public RequestRes close(Long userId, Long requestId) {
        BloodRequest r = requests.findById(requestId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Request not found"));
        if (!r.requesterId.equals(userId)) throw new ApiException(HttpStatus.FORBIDDEN, "Only the requester can close this request");
        r.status = RequestStatus.CLOSED;
        requests.save(r);
        return detail(userId, requestId);
    }

    // ---------- admin ----------
    @Transactional(readOnly = true)
    public List<RequestRes> adminAll() {
        return requests.findAll(Sort.by(Sort.Direction.DESC, "createdAt")).stream().map(r -> {
            List<DonorResponse> rs = responses.findByRequestId(r.id);
            return toRes(r, user(r.requesterId), rs.size(), count(rs, ResponseStatus.ACCEPTED), null, List.of(), null);
        }).toList();
    }

    @Transactional(readOnly = true)
    public RequestRes adminDetail(Long requestId) {
        BloodRequest r = requests.findById(requestId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Request not found"));
        return detail(r.requesterId, requestId);   // owner-style view with accepted donors
    }

    @Transactional
    public RequestRes adminClose(Long requestId) {
        BloodRequest r = requests.findById(requestId)
            .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Request not found"));
        return close(r.requesterId, requestId);
    }

    @Transactional
    public void adminDelete(Long requestId) {
        if (!requests.existsById(requestId)) throw new ApiException(HttpStatus.NOT_FOUND, "Request not found");
        responses.deleteByRequestId(requestId);
        requests.deleteById(requestId);
    }

    // ---------- helpers ----------
    private User user(Long id) {
        return users.findById(id).orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private void notify(Long userId, String title, String message, String link) {
        Notification n = new Notification();
        n.userId = userId; n.title = title; n.message = message; n.link = link;
        notifications.save(n);
    }

    private int count(List<DonorResponse> rs, ResponseStatus s) {
        return (int) rs.stream().filter(x -> x.status == s).count();
    }

    private RequestRes toRes(BloodRequest r, User requester, int notified, int accepted, String myStatus,
                             List<DonorContact> donors, RequesterContact contact) {
        return new RequestRes(r.id, r.patientName, r.bloodGroup, r.units, r.hospital, r.city, r.message,
            r.urgency.name(), r.status.name(), r.createdAt, requester.name, notified, accepted, myStatus, donors, contact);
    }
}
