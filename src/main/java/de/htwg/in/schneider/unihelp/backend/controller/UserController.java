package de.htwg.in.schneider.unihelp.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import de.htwg.in.schneider.unihelp.backend.model.Booking;
import de.htwg.in.schneider.unihelp.backend.model.Offer;
import de.htwg.in.schneider.unihelp.backend.model.User;
import de.htwg.in.schneider.unihelp.backend.repository.BookingRepository;
import de.htwg.in.schneider.unihelp.backend.repository.OfferRepository;
import de.htwg.in.schneider.unihelp.backend.repository.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping("/{oauthId}")
    public ResponseEntity<Map<String, Object>> getPublicProfile(@PathVariable String oauthId) {
        Optional<User> optUser = userRepository.findByOauthId(oauthId);
        if (!optUser.isPresent()) {
            return ResponseEntity.notFound().build();
        }

        User user = optUser.get();
        Map<String, Object> publicData = new HashMap<>();

        publicData.put("oauthId", user.getOauthId());
        publicData.put("firstName", user.getFirstName());
        publicData.put("lastName", user.getLastName());
        publicData.put("email", user.getEmail());
        publicData.put("university", user.getUniversity());
        publicData.put("course", user.getCourse());

        List<Offer> offers = offerRepository.findByOwnerOauthId(oauthId);
        publicData.put("offers", offers);

        List<Booking> bookings = bookingRepository.findByStudentOauthIdOrTutorOauthId(oauthId, oauthId);
        int reviewCount = 0;
        double ratingSum = 0;

        for (Booking booking : bookings) {
            if (oauthId.equals(booking.getTutorOauthId()) && "RATED".equals(booking.getStatus()) && booking.getReview() != null
                    && booking.getReview().getRatingStars() != null) {
                reviewCount++;
                ratingSum += booking.getReview().getRatingStars();
            }
        }

        publicData.put("reviewCount", reviewCount);
        double avg = reviewCount > 0 ? (ratingSum / reviewCount) : 0.0;
        publicData.put("averageRating", Math.round(avg * 10.0) / 10.0);

        return ResponseEntity.ok(publicData);
    }
}
