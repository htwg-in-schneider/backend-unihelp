package de.htwg.in.schneider.unihelp.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    private String university;
    private String course;
    private String module;
    private Double price;
    private String description;
    private String availableTimes;
    private String language;

    @Enumerated(EnumType.STRING)
    private Format format;

    private Boolean isActive;

    public Offer() {
    }

    public Offer(Long id, String university, String course, String module, double price, String description,
            String availableTimes, String language, Format format, boolean isActive) {
        this.id = id;
        this.university = university;
        this.course = course;
        this.module = module;
        this.price = price;
        this.description = description;
        this.availableTimes = availableTimes;
        this.language = language;
        this.format = format;
        this.isActive = isActive;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUniversity() {
        return university;
    }

    public void setUniversity(String university) {
        this.university = university;
    }

    public String getCourse() {
        return course;
    }

    public void setCourse(String course) {
        this.course = course;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAvailableTimes() {
        return availableTimes;
    }

    public void setAvailableTimes(String availableTimes) {
        this.availableTimes = availableTimes;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Format getFormat() {
        return format;
    }

    public void setFormat(Format format) {
        this.format = format;
    }

    public boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Offer offer = (Offer) o;
        return id != null && id.equals(offer.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "Offer{" +
                "id=" + id +
                ", university='" + university + '\'' +
                ", course='" + course + '\'' +
                ", module='" + module + '\'' +
                ", price=" + price +
                ", description='" + description + '\'' +
                ", availableTimes='" + availableTimes + '\'' +
                ", language='" + language + '\'' +
                ", format=" + format +
                ", isActive=" + isActive +
                '}';
    }
}
