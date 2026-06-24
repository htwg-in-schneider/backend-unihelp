package de.htwg.in.schneider.unihelp.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import de.htwg.in.schneider.unihelp.backend.model.Offer;
import de.htwg.in.schneider.unihelp.backend.model.Booking;
import de.htwg.in.schneider.unihelp.backend.model.Format;
import de.htwg.in.schneider.unihelp.backend.model.Role;
import de.htwg.in.schneider.unihelp.backend.model.User;
import de.htwg.in.schneider.unihelp.backend.repository.BookingRepository;
import de.htwg.in.schneider.unihelp.backend.repository.OfferRepository;
import de.htwg.in.schneider.unihelp.backend.repository.UserRepository;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/offer")
public class OfferController {

    private static final Logger LOG = LoggerFactory.getLogger(OfferController.class);

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private boolean isRegisteredUser(Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null) {
            return false;
        }
        Optional<User> user = userRepository.findByOauthId(jwt.getSubject());
        return user.isPresent();
    }

    private boolean isOwnerOrAdmin(Jwt jwt, Offer offer) {
        if (jwt == null || jwt.getSubject() == null) {
            return false;
        }
        Optional<User> user = userRepository.findByOauthId(jwt.getSubject());
        if (user.isPresent() && user.get().getRole() == Role.ADMIN) {
            return true;
        }
        return offer.getOwnerOauthId() != null && offer.getOwnerOauthId().equals(jwt.getSubject());
    }

    private boolean isValidOffer(Offer offer) {
        if (offer == null) {
            return false;
        }
        if (isBlank(offer.getModule()) || isBlank(offer.getUniversity())
                || isBlank(offer.getCourse()) || isBlank(offer.getLanguage())) {
            return false;
        }
        if (offer.getFormat() == null) {
            return false;
        }
        if (offer.getPrice() <= 0) {
            return false;
        }
        if (offer.getAvailabilities() != null) {
            for (var avail : offer.getAvailabilities()) {
                if (avail.getStartTime() != null && avail.getEndTime() != null
                        && !avail.getEndTime().isAfter(avail.getStartTime())) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    @GetMapping
    public List<Offer> getOffers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String format) {

        boolean hasSearch = search != null && !search.trim().isEmpty();

        Format formatEnum = null;
        if (format != null && !format.trim().isEmpty()) {
            try {
                formatEnum = Format.valueOf(format.trim().toUpperCase());
            } catch (IllegalArgumentException e) {
            }
        }
        boolean hasFormat = formatEnum != null;

        List<Format> allowedFormats = new ArrayList<>();
        if (hasFormat) {
            if (formatEnum == Format.ONLINE) {
                allowedFormats.addAll(Arrays.asList(Format.ONLINE, Format.HYBRID));
            } else if (formatEnum == Format.PRAESENZ) {
                allowedFormats.addAll(Arrays.asList(Format.PRAESENZ, Format.HYBRID));
            } else {
                allowedFormats.add(Format.HYBRID);
            }
        }

        if (hasSearch && hasFormat) {
            return offerRepository
                    .findByFormatInAndModuleContainingIgnoreCaseOrFormatInAndCourseContainingIgnoreCaseOrFormatInAndUniversityContainingIgnoreCase(
                            allowedFormats, search, allowedFormats, search, allowedFormats, search);
        } else if (hasSearch) {
            return offerRepository
                    .findByModuleContainingIgnoreCaseOrCourseContainingIgnoreCaseOrUniversityContainingIgnoreCase(
                            search, search, search);
        } else if (hasFormat) {
            return offerRepository.findByFormatIn(allowedFormats);
        } else {
            return offerRepository.findAll();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Offer> getOfferById(@PathVariable Long id) {
        Optional<Offer> opt = offerRepository.findById(id);
        if (opt.isPresent()) {
            return ResponseEntity.ok(opt.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Offer> createOffer(@AuthenticationPrincipal Jwt jwt, @RequestBody Offer offer) {
        if (!isRegisteredUser(jwt)) {
            return ResponseEntity.status(403).build();
        }

        if (!isValidOffer(offer)) {
            return ResponseEntity.badRequest().build();
        }

        if (offer.getId() != null) {
            offer.setId(null);
        }

        offer.setOwnerOauthId(jwt.getSubject());

        Optional<User> optUser = userRepository.findByOauthId(jwt.getSubject());
        if (optUser.isPresent()) {
            offer.setOwnerName(optUser.get().getName());
        } else {
            offer.setOwnerName("Tutor");
        }

        Offer newOffer = offerRepository.save(offer);
        LOG.info("Created new offer with id " + newOffer.getId());
        return ResponseEntity.ok(newOffer);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Offer> updateOffer(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
            @RequestBody Offer offerDetails) {
        if (!isRegisteredUser(jwt)) {
            return ResponseEntity.status(403).build();
        }

        Optional<Offer> opt = offerRepository.findById(id);
        if (!opt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        Offer offer = opt.get();

        if (!isOwnerOrAdmin(jwt, offer)) {
            return ResponseEntity.status(403).build();
        }

        if (!isValidOffer(offerDetails)) {
            return ResponseEntity.badRequest().build();
        }

        offer.setUniversity(offerDetails.getUniversity());
        offer.setCourse(offerDetails.getCourse());
        offer.setModule(offerDetails.getModule());
        offer.setPrice(offerDetails.getPrice());
        offer.setDescription(offerDetails.getDescription());

        offer.setAvailabilities(offerDetails.getAvailabilities());

        offer.setLanguage(offerDetails.getLanguage());
        offer.setFormat(offerDetails.getFormat());
        offer.setIsActive(offerDetails.getIsActive());

        Offer updatedOffer = offerRepository.save(offer);
        LOG.info("Updated offer with id " + updatedOffer.getId());
        return ResponseEntity.ok(updatedOffer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteOffer(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        if (!isRegisteredUser(jwt)) {
            return ResponseEntity.status(403).build();
        }

        Optional<Offer> opt = offerRepository.findById(id);
        if (!opt.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        if (!isOwnerOrAdmin(jwt, opt.get())) {
            return ResponseEntity.status(403).build();
        }

        List<Booking> bookings = bookingRepository.findByOfferId(id);
        bookingRepository.deleteAll(bookings);

        offerRepository.delete(opt.get());
        LOG.info("Deleted offer with id " + id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/reviews")
    public ResponseEntity<List<Map<String, Object>>> getOfferReviews(@PathVariable Long id) {
        List<Booking> bookings = bookingRepository.findByOfferIdAndStatus(id, "RATED");
        List<Map<String, Object>> reviews = new ArrayList<>();

        for (Booking b : bookings) {
            if (b.getReview() != null) {
                Map<String, Object> map = new HashMap<>();
                map.put("id", b.getReview().getId());
                map.put("studentName", b.getStudentName());
                map.put("ratingStars", b.getReview().getRatingStars());
                map.put("ratingComment", b.getReview().getRatingComment());
                map.put("createdAt", b.getReview().getCreatedAt());
                reviews.add(map);
            }
        }
        return ResponseEntity.ok(reviews);
    }
}