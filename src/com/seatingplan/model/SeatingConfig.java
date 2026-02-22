package com.seatingplan.model;

import java.util.*;

/**
 * Configuration class for seating arrangement customization.
 * Holds all options for how students should be seated.
 */
public class SeatingConfig {

    // Fill direction options
    public enum FillDirection {
        ROW_FIRST("Row First (Left to Right)", "Fill row completely before moving to next row"),
        COLUMN_FIRST("Column First (Top to Bottom)", "Fill column completely before moving to next column"),
        ZIGZAG_ROW("Zigzag Row", "Alternate direction each row (L-R, R-L, L-R...)"),
        ZIGZAG_COLUMN("Zigzag Column", "Alternate direction each column (T-B, B-T, T-B...)");

        private final String displayName;
        private final String description;

        FillDirection(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        @Override public String toString() { return displayName; }
    }

    // Starting corner options
    public enum StartCorner {
        TOP_LEFT("Top Left", 0, 0),
        TOP_RIGHT("Top Right", 0, -1),
        BOTTOM_LEFT("Bottom Left", -1, 0),
        BOTTOM_RIGHT("Bottom Right", -1, -1);

        private final String displayName;
        private final int rowMultiplier;
        private final int colMultiplier;

        StartCorner(String displayName, int rowMult, int colMult) {
            this.displayName = displayName;
            this.rowMultiplier = rowMult;
            this.colMultiplier = colMult;
        }

        public String getDisplayName() { return displayName; }
        public int getRowMultiplier() { return rowMultiplier; }
        public int getColMultiplier() { return colMultiplier; }
        @Override public String toString() { return displayName; }
    }

    // Gap/spacing options
    public enum SeatGapping {
        NO_GAP("No Gap", "Fill all seats consecutively"),
        ALTERNATE_SEATS("Alternate Seats", "Leave one seat gap between students"),
        ALTERNATE_ROWS("Alternate Rows", "Leave one row empty between seated rows"),
        CHECKERBOARD("Checkerboard", "Seat in checkerboard pattern (diagonal gaps)");

        private final String displayName;
        private final String description;

        SeatGapping(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        @Override public String toString() { return displayName; }
    }

    // Sorting options for students
    public enum StudentSortOrder {
        ROLL_NUMBER("Roll Number", "Sort by roll number"),
        NAME("Name", "Sort alphabetically by name"),
        BRANCH_THEN_ROLL("Branch → Roll No", "Group by branch, then sort by roll"),
        YEAR_THEN_ROLL("Year → Roll No", "Group by year, then sort by roll"),
        RANDOM("Random", "Randomize student order");

        private final String displayName;
        private final String description;

        StudentSortOrder(String displayName, String description) {
            this.displayName = displayName;
            this.description = description;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        @Override public String toString() { return displayName; }
    }

    // Basic settings
    private FillDirection fillDirection = FillDirection.ROW_FIRST;
    private StartCorner startCorner = StartCorner.TOP_LEFT;
    private SeatGapping seatGapping = SeatGapping.NO_GAP;
    private StudentSortOrder sortOrder = StudentSortOrder.ROLL_NUMBER;

    // Branch-to-room assignment (roomName -> list of allowed branches)
    private Map<String, List<String>> roomBranchAssignments = new HashMap<>();

    // Constraint settings
    private boolean enforceNoSameBranchAdjacent = true;
    private boolean enforceNoSameYearAdjacent = false;
    private boolean enforceNoSameSemesterAdjacent = false;

    // Gap settings
    private int seatGapSize = 1;  // Number of seats to skip
    private int rowGapSize = 1;   // Number of rows to skip

    // Room fill order
    private boolean fillRoomsEvenly = false;  // If true, distribute students evenly across rooms

    // Branch mixing options
    private int maxSameBranchPerRoom = -1;  // -1 means no limit
    private int minBranchesPerRoom = 1;     // Minimum different branches per room

    // Default constructor
    public SeatingConfig() {
    }

    // ==================== BRANCH ASSIGNMENT METHODS ====================

    /**
     * Assign specific branches to a room.
     */
    public void assignBranchesToRoom(String roomName, List<String> branches) {
        roomBranchAssignments.put(roomName, new ArrayList<>(branches));
    }

    /**
     * Add a branch to a room's allowed list.
     */
    public void addBranchToRoom(String roomName, String branch) {
        roomBranchAssignments.computeIfAbsent(roomName, k -> new ArrayList<>()).add(branch);
    }

    /**
     * Get branches assigned to a specific room.
     */
    public List<String> getBranchesForRoom(String roomName) {
        return roomBranchAssignments.getOrDefault(roomName, new ArrayList<>());
    }

    /**
     * Check if a room has specific branch assignments.
     */
    public boolean hasRoomBranchAssignment(String roomName) {
        List<String> branches = roomBranchAssignments.get(roomName);
        return branches != null && !branches.isEmpty();
    }

    /**
     * Check if branch restrictions are configured for any room.
     */
    public boolean hasBranchRestrictions() {
        return !roomBranchAssignments.isEmpty();
    }

    /**
     * Clear all branch assignments.
     */
    public void clearBranchAssignments() {
        roomBranchAssignments.clear();
    }

    /**
     * Get all room-branch assignments.
     */
    public Map<String, List<String>> getAllRoomBranchAssignments() {
        return new HashMap<>(roomBranchAssignments);
    }

    // ==================== GETTERS AND SETTERS ====================

    public FillDirection getFillDirection() {
        return fillDirection;
    }

    public void setFillDirection(FillDirection fillDirection) {
        this.fillDirection = fillDirection;
    }

    public StartCorner getStartCorner() {
        return startCorner;
    }

    public void setStartCorner(StartCorner startCorner) {
        this.startCorner = startCorner;
    }

    public SeatGapping getSeatGapping() {
        return seatGapping;
    }

    public void setSeatGapping(SeatGapping seatGapping) {
        this.seatGapping = seatGapping;
    }

    public StudentSortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(StudentSortOrder sortOrder) {
        this.sortOrder = sortOrder;
    }

    public boolean isEnforceNoSameBranchAdjacent() {
        return enforceNoSameBranchAdjacent;
    }

    public void setEnforceNoSameBranchAdjacent(boolean enforceNoSameBranchAdjacent) {
        this.enforceNoSameBranchAdjacent = enforceNoSameBranchAdjacent;
    }

    public boolean isEnforceNoSameYearAdjacent() {
        return enforceNoSameYearAdjacent;
    }

    public void setEnforceNoSameYearAdjacent(boolean enforceNoSameYearAdjacent) {
        this.enforceNoSameYearAdjacent = enforceNoSameYearAdjacent;
    }

    public boolean isEnforceNoSameSemesterAdjacent() {
        return enforceNoSameSemesterAdjacent;
    }

    public void setEnforceNoSameSemesterAdjacent(boolean enforceNoSameSemesterAdjacent) {
        this.enforceNoSameSemesterAdjacent = enforceNoSameSemesterAdjacent;
    }

    public int getSeatGapSize() {
        return seatGapSize;
    }

    public void setSeatGapSize(int seatGapSize) {
        this.seatGapSize = Math.max(1, seatGapSize);
    }

    public int getRowGapSize() {
        return rowGapSize;
    }

    public void setRowGapSize(int rowGapSize) {
        this.rowGapSize = Math.max(1, rowGapSize);
    }

    public boolean isFillRoomsEvenly() {
        return fillRoomsEvenly;
    }

    public void setFillRoomsEvenly(boolean fillRoomsEvenly) {
        this.fillRoomsEvenly = fillRoomsEvenly;
    }

    public int getMaxSameBranchPerRoom() {
        return maxSameBranchPerRoom;
    }

    public void setMaxSameBranchPerRoom(int maxSameBranchPerRoom) {
        this.maxSameBranchPerRoom = maxSameBranchPerRoom;
    }

    public int getMinBranchesPerRoom() {
        return minBranchesPerRoom;
    }

    public void setMinBranchesPerRoom(int minBranchesPerRoom) {
        this.minBranchesPerRoom = Math.max(1, minBranchesPerRoom);
    }

    @Override
    public String toString() {
        return "SeatingConfig{" +
                "fillDirection=" + fillDirection +
                ", startCorner=" + startCorner +
                ", seatGapping=" + seatGapping +
                ", sortOrder=" + sortOrder +
                ", roomBranchAssignments=" + roomBranchAssignments.size() + " rooms" +
                ", enforceNoSameBranchAdjacent=" + enforceNoSameBranchAdjacent +
                ", enforceNoSameYearAdjacent=" + enforceNoSameYearAdjacent +
                '}';
    }
}
