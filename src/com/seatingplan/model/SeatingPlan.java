package com.seatingplan.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complete seating plan across multiple classrooms.
 */
public class SeatingPlan {
    private List<Classroom> classrooms;
    private List<Student> students;
    private PatternType patternUsed;
    private String examName;
    private String examDate;
    private List<Student> unassignedStudents;

    public SeatingPlan() {
        this.classrooms = new ArrayList<>();
        this.students = new ArrayList<>();
        this.unassignedStudents = new ArrayList<>();
    }

    public SeatingPlan(List<Classroom> classrooms, List<Student> students) {
        this.classrooms = classrooms != null ? classrooms : new ArrayList<>();
        this.students = students != null ? students : new ArrayList<>();
        this.unassignedStudents = new ArrayList<>();
    }

    /**
     * Get total capacity across all classrooms.
     */
    public int getTotalCapacity() {
        return classrooms.stream().mapToInt(Classroom::getCapacity).sum();
    }

    /**
     * Get total occupied seats across all classrooms.
     */
    public int getTotalOccupied() {
        return classrooms.stream().mapToInt(Classroom::getOccupiedCount).sum();
    }

    /**
     * Check if all students can be accommodated.
     */
    public boolean canAccommodateAll() {
        return getTotalCapacity() >= students.size();
    }

    /**
     * Clear all seat assignments in all classrooms.
     */
    public void clearAllAssignments() {
        for (Classroom classroom : classrooms) {
            classroom.clearAllSeats();
        }
        unassignedStudents.clear();
        unassignedStudents.addAll(students);
    }

    /**
     * Get all assigned students with their seat info.
     */
    public List<SeatAssignment> getAllAssignments() {
        List<SeatAssignment> assignments = new ArrayList<>();
        for (Classroom classroom : classrooms) {
            Seat[][] seats = classroom.getSeats();
            for (int r = 0; r < classroom.getRows(); r++) {
                for (int c = 0; c < classroom.getColumns(); c++) {
                    Seat seat = seats[r][c];
                    if (seat != null && seat.isOccupied()) {
                        assignments.add(new SeatAssignment(
                            classroom.getRoomName(),
                            seat.getSeatLabel(),
                            r, c,
                            seat.getAssignedStudent()
                        ));
                    }
                }
            }
        }
        return assignments;
    }

    // Getters and Setters
    public List<Classroom> getClassrooms() {
        return classrooms;
    }

    public void setClassrooms(List<Classroom> classrooms) {
        this.classrooms = classrooms;
    }

    public List<Student> getStudents() {
        return students;
    }

    public void setStudents(List<Student> students) {
        this.students = students;
    }

    public PatternType getPatternUsed() {
        return patternUsed;
    }

    public void setPatternUsed(PatternType patternUsed) {
        this.patternUsed = patternUsed;
    }

    public String getExamName() {
        return examName;
    }

    public void setExamName(String examName) {
        this.examName = examName;
    }

    public String getExamDate() {
        return examDate;
    }

    public void setExamDate(String examDate) {
        this.examDate = examDate;
    }

    public List<Student> getUnassignedStudents() {
        return unassignedStudents;
    }

    public void setUnassignedStudents(List<Student> unassignedStudents) {
        this.unassignedStudents = unassignedStudents;
    }

    /**
     * Inner class to represent a seat assignment for reporting.
     */
    public static class SeatAssignment {
        private String roomName;
        private String seatLabel;
        private int row;
        private int column;
        private Student student;

        public SeatAssignment(String roomName, String seatLabel, int row, int column, Student student) {
            this.roomName = roomName;
            this.seatLabel = seatLabel;
            this.row = row;
            this.column = column;
            this.student = student;
        }

        public String getRoomName() { return roomName; }
        public String getSeatLabel() { return seatLabel; }
        public int getRow() { return row; }
        public int getColumn() { return column; }
        public Student getStudent() { return student; }
    }
}
