package de.htwg.in.schneider.unihelp.backend.controller;

import de.htwg.in.schneider.unihelp.backend.model.*;
import de.htwg.in.schneider.unihelp.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/moderation")
public class ModerationController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private SuspensionRepository suspensionRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private boolean isModerator(Jwt jwt) {
        if (jwt == null)
            return false;
        Optional<User> user = userRepository.findByOauthId(jwt.getSubject());
        return user.isPresent() && (user.get().getRole() == Role.MODERATOR || user.get().getRole() == Role.ADMIN);
    }

    private boolean isAdmin(Jwt jwt) {
        if (jwt == null)
            return false;
        Optional<User> user = userRepository.findByOauthId(jwt.getSubject());
        return user.isPresent() && user.get().getRole() == Role.ADMIN;
    }

    private void closeReportsFor(String targetType, Long targetId) {
        List<Report> allReports = reportRepository.findAll();
        for (Report report : allReports) {
            if (targetType.equals(report.getTargetType()) && report.getTargetId().equals(targetId)) {
                report.setStatus("CLOSED");
                reportRepository.save(report);
            }
        }
    }

    @GetMapping("/reports")
    public ResponseEntity<List<Report>> getReports(@AuthenticationPrincipal Jwt jwt) {
        if (!isModerator(jwt))
            return ResponseEntity.status(403).build();
        return ResponseEntity.ok(reportRepository.findAll());
    }

    @GetMapping("/users")
    public ResponseEntity<List<User>> getAllUsers(@AuthenticationPrincipal Jwt jwt) {
        if (!isModerator(jwt))
            return ResponseEntity.status(403).build();
        return ResponseEntity.ok(userRepository.findAll());
    }

    @GetMapping("/bookings")
    public ResponseEntity<List<Booking>> getAllBookings(@AuthenticationPrincipal Jwt jwt) {
        if (!isAdmin(jwt))
            return ResponseEntity.status(403).build();
        return ResponseEntity.ok(bookingRepository.findAll());
    }

    @GetMapping("/suspensions")
    public ResponseEntity<List<Suspension>> getSuspensions(@AuthenticationPrincipal Jwt jwt) {
        if (!isModerator(jwt))
            return ResponseEntity.status(403).build();

        List<Suspension> allSuspensions = suspensionRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        for (Suspension suspension : allSuspensions) {
            if ("TEMPORARY".equals(suspension.getType()) && suspension.getUntilDate() != null
                    && now.isAfter(suspension.getUntilDate())) {
                suspensionRepository.delete(suspension);
            }
        }
        return ResponseEntity.ok(suspensionRepository.findAll());
    }

    @PostMapping("/reports/close")
    public ResponseEntity<Void> closeReports(@AuthenticationPrincipal Jwt jwt,
            @RequestBody Map<String, Object> payload) {
        if (!isModerator(jwt))
            return ResponseEntity.status(403).build();
        String targetType = (String) payload.get("targetType");
        Long targetId = ((Number) payload.get("targetId")).longValue();

        List<Report> allReports = reportRepository.findAll();
        for (Report report : allReports) {
            if (targetType.equals(report.getTargetType()) && report.getTargetId().equals(targetId)) {
                report.setStatus("CLOSED");
                reportRepository.save(report);
            }
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reports/open")
    public ResponseEntity<Void> openReports(@AuthenticationPrincipal Jwt jwt,
            @RequestBody Map<String, Object> payload) {
        if (!isModerator(jwt))
            return ResponseEntity.status(403).build();
        String targetType = (String) payload.get("targetType");
        Long targetId = ((Number) payload.get("targetId")).longValue();

        List<Report> allReports = reportRepository.findAll();
        for (Report report : allReports) {
            if (targetType.equals(report.getTargetType()) && report.getTargetId().equals(targetId)) {
                report.setStatus("OPEN");
                reportRepository.save(report);
            }
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/reports")
    public ResponseEntity<Report> createReport(@AuthenticationPrincipal Jwt jwt,
            @RequestBody Map<String, Object> payload) {
        if (jwt == null)
            return ResponseEntity.status(401).build();

        Report report = new Report();
        report.setReporterOauthId(jwt.getSubject());
        report.setCreatedAt(LocalDateTime.now());
        report.setStatus("OPEN");
        report.setReason((String) payload.get("reason"));
        report.setTargetType((String) payload.get("targetType"));

        if ("OFFER".equals(report.getTargetType())) {
            report.setTargetId(((Number) payload.get("targetId")).longValue());
        } else if ("USER".equals(report.getTargetType())) {
            String targetOauthId = (String) payload.get("targetOauthId");
            Optional<User> targetUser = userRepository.findByOauthId(targetOauthId);
            if (targetUser.isPresent()) {
                report.setTargetId(targetUser.get().getId());
            } else {
                return ResponseEntity.badRequest().build();
            }
        }

        List<Report> existing = reportRepository.findAll();
        for (Report r : existing) {
            if (report.getTargetType().equals(r.getTargetType())
                    && report.getTargetId() != null
                    && report.getTargetId().equals(r.getTargetId())
                    && jwt.getSubject().equals(r.getReporterOauthId())
                    && "OPEN".equals(r.getStatus())) {
                return ResponseEntity.status(409).build();
            }
        }

        for (Report r : existing) {
            if (report.getTargetType().equals(r.getTargetType())
                    && report.getTargetId() != null
                    && report.getTargetId().equals(r.getTargetId())
                    && "CLOSED".equals(r.getStatus())) {
                r.setStatus("OPEN");
                reportRepository.save(r);
            }
        }

        reportRepository.save(report);
        return ResponseEntity.ok(report);
    }

    @DeleteMapping("/review/{id}")
    public ResponseEntity<Void> deleteReview(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        if (!isModerator(jwt))
            return ResponseEntity.status(403).build();

        List<Booking> bookings = bookingRepository.findAll();
        for (Booking booking : bookings) {
            if (booking.getReview() != null && booking.getReview().getId().equals(id)) {
                booking.setReview(null);
                booking.setStatus("PAID");
                bookingRepository.save(booking);
                return ResponseEntity.noContent().build();
            }
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping("/user/{id}/ban")
    public ResponseEntity<Void> banUser(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
            @RequestBody Suspension suspensionData) {
        if (!isModerator(jwt))
            return ResponseEntity.status(403).build();

        Optional<User> targetUser = userRepository.findById(id);
        if (!targetUser.isPresent())
            return ResponseEntity.notFound().build();

        suspensionData.setUser(targetUser.get());
        suspensionData.setCreatedAt(LocalDateTime.now());
        suspensionRepository.save(suspensionData);

        closeReportsFor("USER", id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/{id}/unban")
    public ResponseEntity<Void> unbanUser(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        if (!isModerator(jwt))
            return ResponseEntity.status(403).build();

        List<Suspension> suspensions = suspensionRepository.findAll();
        for (Suspension suspension : suspensions) {
            if (suspension.getUser() != null && suspension.getUser().getId().equals(id)) {
                suspensionRepository.delete(suspension);
            }
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/{id}/delete")
    public ResponseEntity<Void> deleteUser(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        if (!isModerator(jwt))
            return ResponseEntity.status(403).build();

        Optional<User> targetUser = userRepository.findById(id);
        if (!targetUser.isPresent())
            return ResponseEntity.notFound().build();

        User u = targetUser.get();
        u.setIsDeleted(true);
        userRepository.save(u);

        closeReportsFor("USER", id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/user/{id}/restore")
    public ResponseEntity<Void> restoreUser(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        if (!isModerator(jwt))
            return ResponseEntity.status(403).build();

        Optional<User> targetUser = userRepository.findById(id);
        if (!targetUser.isPresent())
            return ResponseEntity.notFound().build();

        User u = targetUser.get();
        u.setIsDeleted(false);
        userRepository.save(u);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/offer/{id}")
    public ResponseEntity<Void> deleteOffer(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        if (!isModerator(jwt))
            return ResponseEntity.status(403).build();

        List<Booking> bookings = bookingRepository.findByOfferId(id);
        for (Booking booking : bookings) {
            if (!"CANCELLED".equals(booking.getStatus())) {
                booking.setStatus("CANCELLED");
                bookingRepository.save(booking);
            }
        }

        offerRepository.deleteById(id);
        closeReportsFor("OFFER", id);

        return ResponseEntity.noContent().build();
    }
}
