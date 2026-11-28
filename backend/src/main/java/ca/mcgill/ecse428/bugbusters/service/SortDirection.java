package ca.mcgill.ecse428.bugbusters.service;

import org.springframework.data.domain.Sort;

public enum SortDirection {
    ASCENDING(Sort.Direction.ASC),
    DESCENDING(Sort.Direction.DESC);

    private final Sort.Direction springDirection;

    SortDirection(Sort.Direction springDirection) {
        this.springDirection = springDirection;
    }

    public static SortDirection fromString(String raw) {
        String normalized = raw == null ? "" : raw.trim().toLowerCase();
        return switch (normalized) {
            case "ascending", "asc" -> ASCENDING;
            case "descending", "desc" -> DESCENDING;
            default -> throw new IllegalArgumentException("Invalid sort direction");
        };
    }

    public Sort.Direction toSpringDirection() {
        return springDirection;
    }

    public boolean isDescending() {
        return this == DESCENDING;
    }
}
