package de.htwg.in.schneider.unihelp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.htwg.in.schneider.unihelp.backend.model.Offer;

@Repository
public interface OfferRepository extends JpaRepository<Offer, Long> {
}
