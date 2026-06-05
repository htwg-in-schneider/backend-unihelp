package de.htwg.in.schneider.unihelp.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;

import de.htwg.in.schneider.unihelp.backend.model.User;
import de.htwg.in.schneider.unihelp.backend.repository.UserRepository;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    static final Logger LOGGER = LoggerFactory.getLogger(ProfileController.class);

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<User> getProfile(@AuthenticationPrincipal Jwt jwt) {
        String oauthId = jwt.getSubject();
        if (oauthId == null) {
            return ResponseEntity.badRequest().build();
        }
        return userRepository.findByOauthId(oauthId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    public ResponseEntity<User> updateProfile(@AuthenticationPrincipal Jwt jwt, @RequestBody User updatedUser) {
        String oauthId = jwt.getSubject();
        if (oauthId == null) {
            return ResponseEntity.badRequest().build();
        }

        return userRepository.findByOauthId(oauthId).map(user -> {
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