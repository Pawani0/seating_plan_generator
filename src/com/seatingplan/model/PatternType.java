package com.seatingplan.model;

/**
 * Enum representing different seating pattern types.
 */
public enum PatternType {
    SEQUENTIAL("Sequential", "Fill seats in roll number order"),
    ALTERNATE_BRANCH("Alternate Branch", "Interleave students from different branches"),
    ALTERNATE_YEAR_SEM("Alternate Year/Semester", "Interleave students from different years/semesters"),
    RANDOM_SHUFFLE("Random Shuffle", "Randomize with constraints (no same branch/year neighbors)");

    private final String displayName;
    private final String description;

    PatternType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
