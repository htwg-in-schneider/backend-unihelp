package de.htwg.in.schneider.unihelp.backend.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.htwg.in.schneider.unihelp.backend.model.Offer;
import de.htwg.in.schneider.unihelp.backend.model.Format;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {

        List<Offer> findByModuleContainingIgnoreCaseOrCourseContainingIgnoreCaseOrUniversityContainingIgnoreCase(
                        String module, String course, String university);

        List<Offer> findByFormatIn(List<Format> formats);

        List<Offer> findByFormatInAndModuleContainingIgnoreCaseOrFormatInAndCourseContainingIgnoreCaseOrFormatInAndUniversityContainingIgnoreCase(
                        List<Format> format1, String module, List<Format> format2, String course, List<Format> format3,
                        String university);

        List<Offer> findByOwnerOauthId(String ownerOauthId);
}
