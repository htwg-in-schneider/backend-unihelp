package de.htwg.in.schneider.unihelp.backend.model;

public enum Format {
    ONLINE("Online"),
    PRAESENZ("Präsenz"),
    HYBRID("Online & Präsenz");

    private final String displayName;

    Format(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
