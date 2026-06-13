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

    private boolean isModerator(Jwt jwt) {
        if (jwt == null)
            return false;
        Optional<User> user = userRepository.findByOauthId(jwt.getSubject());
        return user.isPresent() && (user.get().getRole() == Role.MODERATOR || user.get().getRole() == Role.ADMIN);
    }

    private void closeReportsFor(String targetType, Long targetId) {
        List<Report> allReports = reportRepository.findAll();
        for (Report r : allReports) {
            if (targetType.equals(r.getTargetType()) && r.getTargetId().equals(targetId)) {
                r.setStatus("CLOSED");
                reportRepository.save(r);
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

    @GetMapping("/suspensions")
    public ResponseEntity<List<Suspension>> getSuspensions(@AuthenticationPrincipal Jwt jwt) {
        if (!isModerator(jwt))
            return ResponseEntity.status(403).build();

        List<Suspension> all = suspensionRepository.findAll();
        LocalDateTime now = LocalDateTime.now();
        for (Suspension s : all) {
            if ("TEMPORARY".equals(s.getType()) && s.getUntilDate() != null && now.isAfter(s.getUntilDate())) {
                suspensionRepository.delete(s);
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
        for (Report r : allReports) {
            if (targetType.equals(r.getTargetType()) && r.getTargetId().equals(targetId)) {
                r.setStatus("CLOSED");
                reportRepository.save(r);
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
        for (Report r : allReports) {
            if (targetType.equals(r.getTargetType()) && r.getTargetId().equals(targetId)) {
                r.setStatus("OPEN");
                reportRepository.save(r);
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

        reportRepository.save(report);
        return ResponseEntity.ok(report);
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
        for (Suspension s : suspensions) {
            if (s.getUser() != null && s.getUser().getId().equals(id)) {
                suspensionRepository.delete(s);
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

        offerRepository.deleteById(id);
        closeReportsFor("OFFER", id);

        return ResponseEntity.noContent().build();
    }
}
