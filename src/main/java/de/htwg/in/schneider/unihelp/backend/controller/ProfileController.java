package de.htwg.in.schneider.unihelp.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import de.htwg.in.schneider.unihelp.backend.model.User;
import de.htwg.in.schneider.unihelp.backend.model.Suspension;
import de.htwg.in.schneider.unihelp.backend.repository.UserRepository;
import de.htwg.in.schneider.unihelp.backend.repository.SuspensionRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    static final Logger LOGGER = LoggerFactory.getLogger(ProfileController.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SuspensionRepository suspensionRepository;

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private ResponseEntity<Map<String, String>> checkBannedOrDeleted(User user) {
        if (Boolean.TRUE.equals(user.getIsDeleted())) {
            Map<String, String> res = new HashMap<>();
            res.put("type", "DELETED");
            return ResponseEntity.status(403).body(res);
        }

        List<Suspension> suspensions = suspensionRepository.findAll();
        for (Suspension s : suspensions) {
            if (s.getUser() != null && s.getUser().getId().equals(user.getId())) {
                if ("PERMANENT".equals(s.getType())) {
                    Map<String, String> res = new HashMap<>();
                    res.put("type", "PERMANENT");
                    res.put("reason", s.getReason() != null ? s.getReason() : "");
                    return ResponseEntity.status(403).body(res);
                }
                if ("TEMPORARY".equals(s.getType()) && s.getUntilDate() != null) {
                    if (!LocalDateTime.now().isAfter(s.getUntilDate())) {
                        Map<String, String> res = new HashMap<>();
                        res.put("type", "TEMPORARY");
                        res.put("untilDate", s.getUntilDate().toString());
                        res.put("reason", s.getReason() != null ? s.getReason() : "");
                        return ResponseEntity.status(403).body(res);
                    } else {
                        suspensionRepository.delete(s);
                    }
                }
            }
        }
        return null;
    }

    @GetMapping
    public ResponseEntity<?> getProfile(@AuthenticationPrincipal Jwt jwt) {
        String oauthId = jwt.getSubject();
        if (oauthId == null) {
            return ResponseEntity.badRequest().build();
        }

        Optional<User> optUser = userRepository.findByOauthId(oauthId);
        if (optUser.isPresent()) {
            User user = optUser.get();
            ResponseEntity<Map<String, String>> banCheck = checkBannedOrDeleted(user);
            if (banCheck != null) {
                return banCheck;
            }
            return ResponseEntity.ok(user);
        }

        return ResponseEntity.notFound().build();
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal Jwt jwt, @RequestBody User updatedUser) {
        String oauthId = jwt.getSubject();
        if (oauthId == null) {
            return ResponseEntity.badRequest().build();
        }

        if (isBlank(updatedUser.getFirstName()) || isBlank(updatedUser.getLastName())
                || isBlank(updatedUser.getUsername()) || isBlank(updatedUser.getEmail())
                || isBlank(updatedUser.getUniversity()) || isBlank(updatedUser.getCourse())
                || isBlank(updatedUser.getLanguage())) {
            return ResponseEntity.badRequest().build();
        }

        return userRepository.findByOauthId(oauthId).map(user -> {
            ResponseEntity<Map<String, String>> banCheck = checkBannedOrDeleted(user);
            if (banCheck != null) {
                return banCheck;
            }

            String newUsername = updatedUser.getUsername();
            if (newUsername != null && !newUsername.equals(user.getUsername())) {
                Optional<User> existing = userRepository.findByUsername(newUsername);
                if (existing.isPresent()) {
                    Map<String, String> res = new HashMap<>();
                    res.put("error", "USERNAME_TAKEN");
                    return ResponseEntity.status(409).body(res);
                }
            }

            user.setFirstName(updatedUser.getFirstName());
            user.setLastName(updatedUser.getLastName());
            user.setUsername(updatedUser.getUsername());
            user.setEmail(updatedUser.getEmail());
            user.setPhone(updatedUser.getPhone());
            user.setCourse(updatedUser.getCourse());
            user.setUniversity(updatedUser.getUniversity());
            user.setLanguage(updatedUser.getLanguage());
            user.setName(updatedUser.getFirstName() + " " + updatedUser.getLastName());

            User savedUser = userRepository.save(user);
            LOGGER.info("Profile updated for user: {}", oauthId);
            return ResponseEntity.ok(savedUser);
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteProfile(@AuthenticationPrincipal Jwt jwt) {
        String oauthId = jwt.getSubject();
        if (oauthId == null) {
            return ResponseEntity.badRequest().build();
        }

        return userRepository.findByOauthId(oauthId).map(user -> {
            user.setIsDeleted(true);
            user.setDeletedAt(LocalDate.now());
            userRepository.save(user);

            LOGGER.info("Profile soft-deleted for user: {}", oauthId);
            return ResponseEntity.ok().<Void>build();
        }).orElse(ResponseEntity.notFound().build());
    }
}