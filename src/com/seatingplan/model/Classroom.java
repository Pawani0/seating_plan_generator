package com.seatingplan.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a classroom with seating arrangement configuration.
 */
public class Classroom {
    private String roomName;
    private int rows;
    private int columns;
    private Seat[][] seats;
    
    // Branch assignments for this room
    private List<String> assignedBranches = new ArrayList<>();
    
    // Custom settings per room
    private int maxStudentsPerBranch = -1;  // -1 means no limit

    public Classroom() {
    }

    public Classroom(String roomName, int rows, int columns) {
        this.roomName = roomName;
        this.rows = rows;
        this.columns = columns;
        initializeSeats();
    }

    /**
     * Initialize the seats grid for this classroom.
     */
    private void initializeSeats() {
        seats = new Seat[rows][columns];
        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < columns; c++) {
                seats[r][c] = new Seat(r, c);
            }
        }
    }

    /**
     * Returns total seating capacity.
     */
    public int getCapacity() {
        return rows * columns;
    }

    /**
     * Returns the number of occupied seats.
     */
    public int getOccupiedCount() {
        int count = 0;
        if (seats != null) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    if (seats[r][c] != null && seats[r][c].isOccupied()) {
                        count++;
                    }
                }
            }
        }
        return count;
    }

    /**
     * Returns the number of empty seats.
     */
    public int getEmptyCount() {
        return getCapacity() - getOccupiedCount();
    }

    /**
     * Get seat at specific position.
     */
    public Seat getSeat(int row, int col) {
        if (row >= 0 && row < rows && col >= 0 && col < columns) {
            return seats[row][col];
        }
        return null;
    }

    /**
     * Clear all seat assignments.
     */
    public void clearAllSeats() {
        if (seats != null) {
            for (int r = 0; r < rows; r++) {
                for (int c = 0; c < columns; c++) {
                    if (seats[r][c] != null) {
                        seats[r][c].clear();
                    }
                }
            }
        }
    }

    // Getters and Setters
    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
        initializeSeats();
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
        initializeSeats();
    }

    public Seat[][] getSeats() {
        return seats;
    }

    // ==================== BRANCH ASSIGNMENT METHODS ====================

    /**
     * Set the branches allowed in this room.
     */
    public void setAssignedBranches(List<String> branches) {
        this.assignedBranches = branches != null ? new ArrayList<>(branches) : new ArrayList<>();
    }

    /**
     * Add a branch to this room's allowed list.
     */
    public void addAssignedBranch(String branch) {
        if (!assignedBranches.contains(branch)) {
            assignedBranches.add(branch);
        }
    }

    /**
     * Get branches assigned to this room.
     */
    public List<String> getAssignedBranches() {
        return new ArrayList<>(assignedBranches);
    }

    /**
     * Check if this room has specific branch assignments.
     */
    public boolean hasBranchRestrictions() {
        return !assignedBranches.isEmpty();
    }

    /**
     * Check if a student's branch is allowed in this room.
     */
    public boolean isBranchAllowed(String branch) {
        if (assignedBranches.isEmpty()) {
            return true;  // No restrictions, all branches allowed
        }
        return assignedBranches.stream()
                .anyMatch(b -> b.equalsIgnoreCase(branch));
    }

    /**
     * Get max students per branch allowed in this room.
     */
    public int getMaxStudentsPerBranch() {
        return maxStudentsPerBranch;
    }

    /**
     * Set max students per branch (-1 for no limit).
     */
    public void setMaxStudentsPerBranch(int max) {
        this.maxStudentsPerBranch = max;
    }

    /**
     * Clear branch assignments.
     */
    public void clearBranchAssignments() {
        assignedBranches.clear();
    }

    /**
     * Get summary of assigned branches as string.
     */
    public String getBranchSummary() {
        if (assignedBranches.isEmpty()) {
            return "All branches";
        }
        return String.join(", ", assignedBranches);
    }

    @Override
    public String toString() {
        return roomName + " (" + rows + "x" + columns + " = " + getCapacity() + " seats)";
    }
}
