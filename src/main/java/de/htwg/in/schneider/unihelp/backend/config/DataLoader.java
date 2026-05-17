package de.htwg.in.schneider.unihelp.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import de.htwg.in.schneider.unihelp.backend.model.Offer;
import de.htwg.in.schneider.unihelp.backend.repository.OfferRepository;

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
        dbis.setAvailableTimes("Sa, So 10:00 - 12:00");
        dbis.setLanguage("Deutsch");
        dbis.setFormat("Online & Präsenz");
        dbis.setIsActive(true);
        
        Offer math = new Offer();
        math.setUniversity("HTWG Konstanz");
        math.setCourse("Wirtschaftsinformatik");
        math.setModule("Mathematik für Wirtschaftsinformatik 1 & 2");
        math.setPrice(12.00);
        math.setDescription("Mathe ist gar nicht so schwer, wir werden alle Altklausuren durchrechnen.");
        math.setAvailableTimes("Mo, Mi 18:00 - 20:00");
        math.setLanguage("Deutsch");
        math.setFormat("Präsenz & Online");
        math.setIsActive(true);
        
        Offer stat = new Offer();
        stat.setUniversity("HTWG Konstanz");
        stat.setCourse("Wirtschaftsinformatik");
        stat.setModule("Statistik");
        stat.setPrice(10.00);
        stat.setDescription("Glaube nie einer Statistik, die du nicht selbst gefälscht hast. Ich zeige dir, wie du die Klausur bestehst.");
        stat.setAvailableTimes("Fr 14:00 - 18:00");
        stat.setLanguage("Deutsch");
        stat.setFormat("Präsenz");
        stat.setIsActive(true);

        repository.saveAll(Arrays.asList(dbis, math, stat));
        LOGGER.info("Initial data loaded successfully.");
    }
}
