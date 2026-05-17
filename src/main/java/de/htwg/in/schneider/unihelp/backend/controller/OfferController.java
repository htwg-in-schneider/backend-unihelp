package de.htwg.in.schneider.unihelp.backend.controller;

import de.htwg.in.schneider.unihelp.backend.model.Offer;
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

    @GetMapping
    public List<Offer> getOffers() {
        Offer dbis = new Offer();
        dbis.setId(1L);
        dbis.setUniversity("HTWG Konstanz");
        dbis.setCourse("Wirtschaftsinformatik");
        dbis.setModule("Datenbank- und Informationssysteme 1");
        dbis.setPrice(15.00);
        dbis.setDescription("Ich helfe dir gerne bei SQL und Datenbankentwürfen. :)");
        dbis.setAvailableTimes("Sa, So 10:00 - 12:00");
        dbis.setLanguage("Deutsch");
        dbis.setFormat("Online & Präsenz");
        dbis.setIsActive(true);

        Offer math = new Offer();
        math.setId(2L);
        math.setUniversity("HTWG Konstanz");
        math.setCourse("Wirtschaftsinformatik");
        math.setModule("Mathematik für Wirtschaftsinformatik 1 & 2");
        math.setPrice(12.00);
        math.setDescription("Mathe ist gar nicht so schwer, wir werden alle Altklausuren durchrechnen.");
        math.setAvailableTimes("Mo, Mi 18:00 - 20:00");
        math.setLanguage("Deutsch");
        math.setFormat("Präsenz & Online");
        math.setIsActive(true);

        return Arrays.asList(dbis, math);
    }

    @PostMapping
    public ResponseEntity<String> createOffer(@RequestBody Offer offer) {
        System.out.println("Controller called for offer: " + offer.getModule() + " - " + offer.getDescription());
        return ResponseEntity.ok("POST successful");
    }
}