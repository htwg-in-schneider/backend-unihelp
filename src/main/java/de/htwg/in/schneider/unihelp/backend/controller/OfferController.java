package de.htwg.in.schneider.unihelp.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import de.htwg.in.schneider.unihelp.backend.model.Offer;
import de.htwg.in.schneider.unihelp.backend.model.Format;
import de.htwg.in.schneider.unihelp.backend.repository.OfferRepository;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Optional;

@RestController
@RequestMapping("/api/offer")
public class OfferController {

    private static final Logger LOG = LoggerFactory.getLogger(OfferController.class);

    @Autowired
    private OfferRepository offerRepository;

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
            return offerRepository.findByFormatInAndModuleContainingIgnoreCaseOrFormatInAndCourseContainingIgnoreCaseOrFormatInAndUniversityContainingIgnoreCase(
                allowedFormats, search, allowedFormats, search, allowedFormats, search
            );
        } 
        else if (hasSearch) {
            return offerRepository.findByModuleContainingIgnoreCaseOrCourseContainingIgnoreCaseOrUniversityContainingIgnoreCase(search, search, search);
        } 
        else if (hasFormat) {
            return offerRepository.findByFormatIn(allowedFormats); 
        } 
        else {
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
    public Offer createOffer(@RequestBody Offer offer) {
        if (offer.getId() != null) {
            offer.setId(null);
            LOG.warn(
                    "Attempted to create an offer with an existing ID. ID has been set to null to create a new offer.");
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