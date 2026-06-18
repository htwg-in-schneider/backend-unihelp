package de.htwg.in.schneider.unihelp.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import de.htwg.in.schneider.unihelp.backend.model.Booking;
import de.htwg.in.schneider.unihelp.backend.model.Availability;
import de.htwg.in.schneider.unihelp.backend.model.User;
import de.htwg.in.schneider.unihelp.backend.model.Review;
import de.htwg.in.schneider.unihelp.backend.repository.BookingRepository;
import de.htwg.in.schneider.unihelp.backend.repository.AvailabilityRepository;
import de.htwg.in.schneider.unihelp.backend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/booking")
public class BookingController {

    private static final Logger LOG = LoggerFactory.getLogger(BookingController.class);

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private AvailabilityRepository availabilityRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<List<Booking>> getMyBookings(@AuthenticationPrincipal Jwt jwt) {
        if (jwt == null || jwt.getSubject() == null)
            return ResponseEntity.status(403).build();
        String userId = jwt.getSubject();
        List<Booking> myBookings = bookingRepository.findByStudentOauthIdOrTutorOauthId(userId, userId);
        return ResponseEntity.ok(myBookings);
    }

    @PostMapping
    public ResponseEntity<Booking> createBooking(@AuthenticationPrincipal Jwt jwt,
            @RequestBody Booking bookingRequest) {
        if (jwt == null || jwt.getSubject() == null)
            return ResponseEntity.status(403).build();

        Optional<Availability> optAvail = availabilityRepository.findById(bookingRequest.getAvailability().getId());
        if (!optAvail.isPresent() || optAvail.get().isBooked())
            return ResponseEntity.badRequest().build();

        Availability avail = optAvail.get();
        avail.setBooked(true);
        availabilityRepository.save(avail);

        Booking newBooking = new Booking();
        newBooking.setStudentOauthId(jwt.getSubject());
        newBooking.setTutorOauthId(avail.getOffer().getOwnerOauthId());

        Optional<User> optStudent = userRepository.findByOauthId(jwt.getSubject());
        Optional<User> optTutor = userRepository.findByOauthId(avail.getOffer().getOwnerOauthId());

        newBooking.setStudentName(optStudent.isPresent() ? optStudent.get().getName() : "Student");
        newBooking.setTutorName(optTutor.isPresent() ? optTutor.get().getName() : "Tutor");

        newBooking.setOffer(avail.getOffer());
        newBooking.setAvailability(avail);
        newBooking.setMessageToTutor(bookingRequest.getMessageToTutor());
        newBooking.setStatus("UNPAID");
        newBooking.setBookedAt(LocalDateTime.now());

        Booking savedBooking = bookingRepository.save(newBooking);
        LOG.info("User {} booked availability {}", jwt.getSubject(), avail.getId());

        return ResponseEntity.ok(savedBooking);
    }

    @PutMapping("/{id}/pay")
    public ResponseEntity<Booking> payBooking(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
            @RequestBody Booking paymentRequest) {
        Optional<Booking> opt = bookingRepository.findById(id);
        if (!opt.isPresent())
            return ResponseEntity.notFound().build();

        Booking booking = opt.get();
        if (!booking.getStudentOauthId().equals(jwt.getSubject()))
            return ResponseEntity.status(403).build();

        booking.setStatus("PAID");
        booking.setPaymentMethod(paymentRequest.getPaymentMethod());

        return ResponseEntity.ok(bookingRepository.save(booking));
    }

    @PutMapping("/{id}/rate")
    public ResponseEntity<Booking> rateBooking(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id,
            @RequestBody Review ratingRequest) {
        Optional<Booking> opt = bookingRepository.findById(id);
        if (!opt.isPresent())
            return ResponseEntity.notFound().build();

        Booking booking = opt.get();
        if (!booking.getStudentOauthId().equals(jwt.getSubject()))
            return ResponseEntity.status(403).build();

        booking.setStatus("RATED");
        ratingRequest.setCreatedAt(LocalDateTime.now());

        booking.setReview(ratingRequest);

        return ResponseEntity.ok(bookingRepository.save(booking));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(@AuthenticationPrincipal Jwt jwt, @PathVariable Long id) {
        if (jwt == null || jwt.getSubject() == null)
            return ResponseEntity.status(403).build();

        Optional<Booking> opt = bookingRepository.findById(id);
        if (!opt.isPresent())
            return ResponseEntity.notFound().build();

        Booking booking = opt.get();
        String userId = jwt.getSubject();

        if (!booking.getStudentOauthId().equals(userId) && !booking.getTutorOauthId().equals(userId)) {
            return ResponseEntity.status(403).build();
        }

        Availability avail = booking.getAvailability();
        if (avail != null) {
            avail.setBooked(false);
            availabilityRepository.save(avail);
        }

        bookingRepository.delete(booking);
        LOG.info("User {} cancelled booking {}", userId, id);

        return ResponseEntity.noContent().build();
    }
}
