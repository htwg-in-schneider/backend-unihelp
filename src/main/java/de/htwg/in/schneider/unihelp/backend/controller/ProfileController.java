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

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    static final Logger LOGGER = LoggerFactory.getLogger(ProfileController.class);

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<User> getProfile(@AuthenticationPrincipal Jwt jwt) {
        String oauthId = jwt.getSubject();
        LOGGER.info("getProfile called for principal: {}", oauthId);

        if (oauthId == null) {
            return ResponseEntity.badRequest().build();
        }
        return userRepository.findByOauthId(oauthId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
