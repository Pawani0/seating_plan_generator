package com.seatingplan.model;

/**
 * Represents a single seat in a classroom.
 */
public class Seat {
    private int row;
    private int column;
    private Student assignedStudent;

    public Seat() {
    }

    public Seat(int row, int column) {
        this.row = row;
        this.column = column;
        this.assignedStudent = null;
    }

    /**
     * Check if seat is occupied.
     */
    public boolean isOccupied() {
        return assignedStudent != null;
    }

    /**
     * Assign a student to this seat.
     */
    public void assignStudent(Student student) {
        this.assignedStudent = student;
    }

    /**
     * Clear the seat assignment.
     */
    public void clear() {
        this.assignedStudent = null;
    }

    /**
     * Get seat label (e.g., "R1C2" for row 1, column 2).
     */
    public String getSeatLabel() {
        return "R" + (row + 1) + "C" + (column + 1);
    }

    // Getters and Setters
    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public Student getAssignedStudent() {
        return assignedStudent;
    }

    public void setAssignedStudent(Student assignedStudent) {
        this.assignedStudent = assignedStudent;
    }

    @Override
    public String toString() {
        if (assignedStudent != null) {
            return getSeatLabel() + ": " + assignedStudent.getRollNo();
        }
        return getSeatLabel() + ": [Empty]";
    }
}
