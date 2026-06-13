package de.htwg.in.schneider.unihelp.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import de.htwg.in.schneider.unihelp.backend.model.Availability;

@Repository
public interface AvailabilityRepository extends JpaRepository<Availability, Long> {
}
