package de.htwg.in.schneider.unihelp.backend.controller;

import de.htwg.in.schneider.unihelp.backend.model.Offer;
import de.htwg.in.schneider.unihelp.backend.repository.OfferRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/offer")
public class OfferController {

    @Autowired
    private OfferRepository offerRepository;

    @GetMapping
    public List<Offer> getOffers() {
        return offerRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<String> createOffer(@RequestBody Offer offer) {
        System.out.println("Controller called for offer: " + offer.getModule() + " - " + offer.getDescription());
        return ResponseEntity.ok("POST successful");
    }
}