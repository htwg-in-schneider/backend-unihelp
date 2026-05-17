package de.htwg.in.schneider.unihelp.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/offer")
public class OfferController {

    @GetMapping
    public List<String> getOffers() {
        return Arrays.asList("Datenbank- und Informationssysteme 1",
                "Mathematik für Wirtschaftsinformatik 1 & 2",
                "Statistik");
    }
}