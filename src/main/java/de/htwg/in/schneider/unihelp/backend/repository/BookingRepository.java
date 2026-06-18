package de.htwg.in.schneider.unihelp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.htwg.in.schneider.unihelp.backend.model.Booking;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findByStudentOauthIdOrTutorOauthId(String studentOauthId, String tutorOauthId);

    List<Booking> findByOfferIdAndStatus(Long offerId, String status);
}
