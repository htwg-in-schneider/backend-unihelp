package de.htwg.in.schneider.unihelp.backend.model;

public class Offer {
    private long id;
    private String university;
    private String course;
    private String module;
    private double price;
    private String description;
    private String availableTimes;
    private String language;
    private String format;
    private boolean isActive;

    public Offer() {}

    public Offer(long id, String university, String course, String module, double price, String description, String availableTimes, String language, String format, boolean isActive) {
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

    public long getId() {
        return id; 
    }
    public void setId(long id) { 
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

    public String getFormat() { 
        return format; 
    }

    public void setFormat(String format) { 
        this.format = format; 
    }

    public boolean getIsActive() { 
        return isActive; 
    }
    public void setIsActive(boolean isActive) { 
        this.isActive = isActive; 
    }
}
