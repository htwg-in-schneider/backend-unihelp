package de.htwg.in.schneider.unihelp.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/offer")
public class OfferController {

    public static class Offer {
        private String module;
        private String description;

        public Offer(String module, String description) {
            this.module = module;
            this.description = description;
        }

        public String getModule() {
            return module;
        }

        public String getDescription() {
            return description;
        }
    }

    @GetMapping
    public List<Offer> getOffers() {
        return Arrays.asList(
                new Offer("Datenbank- und Informationssysteme 1", "Hilfe bei SQL und ER-Modellen"),
                new Offer("Mathematik für Wirtschaftsinformatik 1 & 2", "Mathematik ist nicht schwer!"),
                new Offer("Statistik", "Kombinatorik und Wahrscheinlichkeitsrechnung unkompliziert erklärt")
        );
    }

    @PostMapping
    public ResponseEntity<String> createOffer(@RequestBody Offer offer) {
        System.out.println("Controller called for offer: " + offer.getModule() + " - " + offer.getDescription());
        return ResponseEntity.ok("POST successful");
    }
}