package ca.mcgill.ecse428.bugbusters.service;

import org.springframework.data.domain.Sort;

/**
 * ID017: Direction in which movies can be sorted (ascending or descending).
 */
public enum SortDirection {
    ASCENDING(Sort.Direction.ASC),
    DESCENDING(Sort.Direction.DESC);

    private final Sort.Direction springDirection;

    SortDirection(Sort.Direction springDirection) {
        this.springDirection = springDirection;
    }

    /**
     * Parses a user-supplied direction. Accepts "ascending"/"asc" and "descending"/"desc",
     * ignoring case and surrounding whitespace.
     *
     * @throws IllegalArgumentException with message "Invalid sort direction" for anything else
     */
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
