package com.seatingplan.service;

import com.seatingplan.model.Classroom;
import com.seatingplan.model.Seat;
import com.seatingplan.model.Student;

/**
 * Utility class to validate neighbor constraints in seating arrangements.
 * Checks 4-directional adjacency: left, right, front, back (not diagonal).
 */
public class NeighborValidator {

    /**
     * Check if placing a student at the given seat violates branch constraint.
     * Returns true if ANY adjacent seat has a student from the SAME branch.
     */
    public static boolean hasSameBranchNeighbor(Classroom classroom, int row, int col, Student student) {
        // Check left neighbor
        if (col > 0) {
            Seat left = classroom.getSeat(row, col - 1);
            if (left != null && left.isOccupied()) {
                if (left.getAssignedStudent().getBranch().equals(student.getBranch())) {
                    return true;
                }
            }
        }

        // Check right neighbor
        if (col < classroom.getColumns() - 1) {
            Seat right = classroom.getSeat(row, col + 1);
            if (right != null && right.isOccupied()) {
                if (right.getAssignedStudent().getBranch().equals(student.getBranch())) {
                    return true;
                }
            }
        }

        // Check front neighbor (previous row)
        if (row > 0) {
            Seat front = classroom.getSeat(row - 1, col);
            if (front != null && front.isOccupied()) {
                if (front.getAssignedStudent().getBranch().equals(student.getBranch())) {
                    return true;
                }
            }
        }

        // Check back neighbor (next row)
        if (row < classroom.getRows() - 1) {
            Seat back = classroom.getSeat(row + 1, col);
            if (back != null && back.isOccupied()) {
                if (back.getAssignedStudent().getBranch().equals(student.getBranch())) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check if placing a student at the given seat violates year constraint (ignoring semester).
     * Returns true if ANY adjacent seat has a student from the SAME year.
     */
    public static boolean hasSameYearNeighbor(Classroom classroom, int row, int col, Student student) {
        int studentYear = student.getYear();
        
        // Check all 4 directions
        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
        
        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            
            if (newRow >= 0 && newRow < classroom.getRows() &&
                newCol >= 0 && newCol < classroom.getColumns()) {
                
                Seat neighbor = classroom.getSeat(newRow, newCol);
                if (neighbor != null && neighbor.isOccupied()) {
                    if (neighbor.getAssignedStudent().getYear() == studentYear) {
                        return true;
                    }
                }
            }
        }
        
        return false;
    }

    /**
     * Check if placing a student at the given seat violates year/semester constraint.
     * Returns true if ANY adjacent seat has a student from the SAME year AND semester.
     */
    public static boolean hasSameYearSemNeighbor(Classroom classroom, int row, int col, Student student) {
        String studentKey = student.getYearSemKey();

        // Check left neighbor
        if (col > 0) {
            Seat left = classroom.getSeat(row, col - 1);
            if (left != null && left.isOccupied()) {
                if (left.getAssignedStudent().getYearSemKey().equals(studentKey)) {
                    return true;
                }
            }
        }

        // Check right neighbor
        if (col < classroom.getColumns() - 1) {
            Seat right = classroom.getSeat(row, col + 1);
            if (right != null && right.isOccupied()) {
                if (right.getAssignedStudent().getYearSemKey().equals(studentKey)) {
                    return true;
                }
            }
        }

        // Check front neighbor
        if (row > 0) {
            Seat front = classroom.getSeat(row - 1, col);
            if (front != null && front.isOccupied()) {
                if (front.getAssignedStudent().getYearSemKey().equals(studentKey)) {
                    return true;
                }
            }
        }

        // Check back neighbor
        if (row < classroom.getRows() - 1) {
            Seat back = classroom.getSeat(row + 1, col);
            if (back != null && back.isOccupied()) {
                if (back.getAssignedStudent().getYearSemKey().equals(studentKey)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Check if placing a student violates BOTH branch AND year/sem constraints.
     * Returns true if ANY adjacent seat has a student with same branch OR same year/sem.
     */
    public static boolean hasConflictingNeighbor(Classroom classroom, int row, int col, Student student) {
        return hasSameBranchNeighbor(classroom, row, col, student) ||
               hasSameYearSemNeighbor(classroom, row, col, student);
    }

    /**
     * Count the number of constraint violations for a student at a given position.
     */
    public static int countViolations(Classroom classroom, int row, int col, Student student) {
        int violations = 0;

        int[][] directions = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}}; // left, right, front, back

        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];

            if (newRow >= 0 && newRow < classroom.getRows() &&
                newCol >= 0 && newCol < classroom.getColumns()) {

                Seat neighbor = classroom.getSeat(newRow, newCol);
                if (neighbor != null && neighbor.isOccupied()) {
                    Student neighborStudent = neighbor.getAssignedStudent();

                    if (neighborStudent.getBranch().equals(student.getBranch())) {
                        violations++;
                    }
                    if (neighborStudent.getYearSemKey().equals(student.getYearSemKey())) {
                        violations++;
                    }
                }
            }
        }

        return violations;
    }
}
