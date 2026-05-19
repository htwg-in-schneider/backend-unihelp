package de.htwg.in.schneider.unihelp.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.htwg.in.schneider.unihelp.backend.model.Offer;
import de.htwg.in.schneider.unihelp.backend.model.Format;
import de.htwg.in.schneider.unihelp.backend.model.Availability;
import de.htwg.in.schneider.unihelp.backend.repository.OfferRepository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class DataLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataLoader.class);

    @Bean
    public CommandLineRunner loadData(OfferRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                LOGGER.info("Database is empty. Loading initial UniHelp data...");
                loadInitialData(repository);
            } else {
                LOGGER.info("Database already contains data. Skipping data loading.");
            }
        };
    }

    private void loadInitialData(OfferRepository repository) {
        Offer dbis = new Offer();
        dbis.setUniversity("HTWG Konstanz");
        dbis.setCourse("Wirtschaftsinformatik");
        dbis.setModule("Datenbank- und Informationssysteme 1");
        dbis.setPrice(15.00);
        dbis.setDescription("Ich helfe dir gerne bei SQL und Datenbankentwürfen. :)");
        dbis.setLanguage("Deutsch");
        dbis.setFormat(Format.HYBRID);
        dbis.setIsActive(true);
        dbis.addAvailability(
                new Availability(LocalDate.of(2026, 5, 16), LocalTime.of(10, 0), LocalTime.of(12, 0), false));
        dbis.addAvailability(
                new Availability(LocalDate.of(2026, 5, 17), LocalTime.of(10, 0), LocalTime.of(12, 0), false));

        Offer math = new Offer();
        math.setUniversity("HTWG Konstanz");
        math.setCourse("Wirtschaftsinformatik");
        math.setModule("Mathematik für Wirtschaftsinformatik 1 & 2");
        math.setPrice(12.00);
        math.setDescription("Mathe ist gar nicht so schwer, wir werden alle Altklausuren durchrechnen.");
        math.setLanguage("Deutsch");
        math.setFormat(Format.HYBRID);
        math.setIsActive(true);
        math.addAvailability(
                new Availability(LocalDate.of(2026, 5, 18), LocalTime.of(18, 0), LocalTime.of(20, 0), true));
        math.addAvailability(
                new Availability(LocalDate.of(2026, 5, 20), LocalTime.of(18, 0), LocalTime.of(20, 0), false));

        Offer stat = new Offer();
        stat.setUniversity("HTWG Konstanz");
        stat.setCourse("Wirtschaftsinformatik");
        stat.setModule("Statistik");
        stat.setPrice(10.00);
        stat.setDescription(
                "Glaube nie einer Statistik, die du nicht selbst gefälscht hast. Ich zeige dir, wie du die Klausur bestehst.");
        stat.setLanguage("Deutsch");
        stat.setFormat(Format.PRAESENZ);
        stat.setIsActive(true);
        stat.addAvailability(
                new Availability(LocalDate.of(2026, 5, 22), LocalTime.of(14, 0), LocalTime.of(18, 0), false));

        repository.saveAll(Arrays.asList(dbis, math, stat));
        LOGGER.info("Initial data loaded successfully.");
    }
}
