package de.htwg.in.schneider.unihelp.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import de.htwg.in.schneider.unihelp.backend.model.Offer;
import de.htwg.in.schneider.unihelp.backend.repository.OfferRepository;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/offer")
public class OfferController {

    private static final Logger LOG = LoggerFactory.getLogger(OfferController.class);

    @Autowired
    private OfferRepository offerRepository;

    @GetMapping
    public List<Offer> getOffers() {
        return offerRepository.findAll();
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
    public Offer createOffer(@RequestBody Offer offer) {
        if (offer.getId() != null) {
            offer.setId(null);
            LOG.warn("Attempted to create an offer with an existing ID. ID has been set to null to create a new offer.");
        }
        Offer newOffer = offerRepository.save(offer);
        LOG.info("Created new offer with id " + newOffer.getId());
        return newOffer;
    }

    @PutMapping("/{id}")
    public ResponseEntity<Offer> updateOffer(@PathVariable Long id, @RequestBody Offer offerDetails) {
        Optional<Offer> opt = offerRepository.findById(id);
        if (!opt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        Offer offer = opt.get();
        offer.setUniversity(offerDetails.getUniversity());
        offer.setCourse(offerDetails.getCourse());
        offer.setModule(offerDetails.getModule());
        offer.setPrice(offerDetails.getPrice());
        offer.setDescription(offerDetails.getDescription());
        offer.setAvailableTimes(offerDetails.getAvailableTimes());
        offer.setLanguage(offerDetails.getLanguage());
        offer.setFormat(offerDetails.getFormat());
        offer.setIsActive(offerDetails.getIsActive());

        Offer updatedOffer = offerRepository.save(offer);
        LOG.info("Updated offer with id " + updatedOffer.getId());
        return ResponseEntity.ok(updatedOffer);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteOffer(@PathVariable Long id) {
        Optional<Offer> opt = offerRepository.findById(id);
        if (!opt.isPresent()) {
            return ResponseEntity.notFound().build();
        }
        offerRepository.delete(opt.get());
        LOG.info("Deleted offer with id " + id);
        return ResponseEntity.noContent().build(); 
    }
}