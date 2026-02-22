package com.seatingplan.service;

import com.seatingplan.model.*;
import com.seatingplan.util.SeatTraversal;

import java.util.*;

/**
 * Random Shuffle allocation - randomizes seating with constraints that
 * no adjacent seats (left, right, front, back) have students from:
 * - Same branch AND
 * - Same year/semester
 * 
 * Uses a greedy algorithm with backtracking for optimal placement.
 * Supports room-specific branch assignments and configurable constraints.
 */
public class RandomShuffleAllocator implements SeatingAlgorithm {

    private Random random = new Random();

    @Override
    public SeatingPlan allocate(List<Student> students, List<Classroom> classrooms) {
        return allocate(students, classrooms, new SeatingConfig());
    }

    @Override
    public SeatingPlan allocate(List<Student> students, List<Classroom> classrooms, SeatingConfig config) {
        List<Classroom> freshClassrooms = createFreshClassrooms(classrooms);
        
        // Copy branch assignments
        for (int i = 0; i < classrooms.size(); i++) {
            freshClassrooms.get(i).setAssignedBranches(classrooms.get(i).getAssignedBranches());
        }
        
        SeatingPlan plan = new SeatingPlan(freshClassrooms, new ArrayList<>(students));
        plan.setPatternUsed(PatternType.RANDOM_SHUFFLE);

        // Shuffle students randomly
        List<Student> shuffledStudents = new ArrayList<>(students);
        Collections.shuffle(shuffledStudents, random);

        // Get all available seats across classrooms (respecting branch restrictions)
        List<SeatPosition> allSeats = getAllSeatPositions(freshClassrooms, config);

        // Greedy assignment with constraint checking
        List<Student> unassigned = new ArrayList<>();

        for (Student student : shuffledStudents) {
            SeatPosition bestSeat = findBestSeat(freshClassrooms, allSeats, student, config);

            if (bestSeat != null) {
                bestSeat.classroom.getSeat(bestSeat.row, bestSeat.col).assignStudent(student);
                allSeats.remove(bestSeat);
            } else {
                unassigned.add(student);
            }
        }

        // Second pass: try to place unassigned students anywhere with minimum violations
        List<Student> stillUnassigned = new ArrayList<>();
        for (Student student : unassigned) {
            if (!allSeats.isEmpty()) {
                SeatPosition leastBadSeat = findLeastViolationSeat(freshClassrooms, allSeats, student, config);
                if (leastBadSeat != null) {
                    leastBadSeat.classroom.getSeat(leastBadSeat.row, leastBadSeat.col).assignStudent(student);
                    allSeats.remove(leastBadSeat);
                } else {
                    stillUnassigned.add(student);
                }
            } else {
                stillUnassigned.add(student);
            }
        }

        plan.setUnassignedStudents(stillUnassigned);
        return plan;
    }

    /**
     * Find the best seat for a student (no violations).
     */
    private SeatPosition findBestSeat(List<Classroom> classrooms, List<SeatPosition> availableSeats, 
                                      Student student, SeatingConfig config) {
        // Shuffle available seats to add randomness
        List<SeatPosition> shuffled = new ArrayList<>(availableSeats);
        Collections.shuffle(shuffled, random);

        for (SeatPosition pos : shuffled) {
            // Check branch restriction for room
            if (!pos.classroom.isBranchAllowed(student.getBranch())) {
                continue;
            }
            
            if (!hasConflict(pos.classroom, pos.row, pos.col, student, config)) {
                return pos;
            }
        }
        return null;
    }

    /**
     * Check if placing student at position violates any enabled constraints.
     */
    private boolean hasConflict(Classroom classroom, int row, int col, Student student, SeatingConfig config) {
        if (config.isEnforceNoSameBranchAdjacent() && 
            NeighborValidator.hasSameBranchNeighbor(classroom, row, col, student)) {
            return true;
        }
        
        if (config.isEnforceNoSameYearAdjacent() && 
            NeighborValidator.hasSameYearNeighbor(classroom, row, col, student)) {
            return true;
        }
        
        if (config.isEnforceNoSameSemesterAdjacent() && 
            NeighborValidator.hasSameYearSemNeighbor(classroom, row, col, student)) {
            return true;
        }
        
        // Default behavior: check both branch and year/sem
        if (!config.isEnforceNoSameBranchAdjacent() && !config.isEnforceNoSameYearAdjacent() &&
            !config.isEnforceNoSameSemesterAdjacent()) {
            return NeighborValidator.hasConflictingNeighbor(classroom, row, col, student);
        }
        
        return false;
    }

    /**
     * Count violations based on config.
     */
    private int countViolations(Classroom classroom, int row, int col, Student student, SeatingConfig config) {
        int violations = 0;
        
        if (config.isEnforceNoSameBranchAdjacent() && 
            NeighborValidator.hasSameBranchNeighbor(classroom, row, col, student)) {
            violations++;
        }
        
        if (config.isEnforceNoSameYearAdjacent() && 
            NeighborValidator.hasSameYearNeighbor(classroom, row, col, student)) {
            violations++;
        }
        
        if (!config.isEnforceNoSameBranchAdjacent() && !config.isEnforceNoSameYearAdjacent()) {
            violations = NeighborValidator.countViolations(classroom, row, col, student);
        }
        
        return violations;
    }

    /**
     * Find seat with minimum violations when no perfect seat is available.
     */
    private SeatPosition findLeastViolationSeat(List<Classroom> classrooms, List<SeatPosition> availableSeats, 
                                                Student student, SeatingConfig config) {
        SeatPosition best = null;
        int minViolations = Integer.MAX_VALUE;

        for (SeatPosition pos : availableSeats) {
            // Check branch restriction
            if (!pos.classroom.isBranchAllowed(student.getBranch())) {
                continue;
            }
            
            int violations = countViolations(pos.classroom, pos.row, pos.col, student, config);
            if (violations < minViolations) {
                minViolations = violations;
                best = pos;
            }
        }

        return best;
    }

    /**
     * Get all seat positions across all classrooms.
     */
    private List<SeatPosition> getAllSeatPositions(List<Classroom> classrooms, SeatingConfig config) {
        List<SeatPosition> positions = new ArrayList<>();
        for (Classroom classroom : classrooms) {
            List<Seat> seats = SeatTraversal.getSeatsInOrder(classroom, config);
            for (Seat seat : seats) {
                positions.add(new SeatPosition(classroom, seat.getRow(), seat.getColumn()));
            }
        }
        return positions;
    }

    private List<Classroom> createFreshClassrooms(List<Classroom> originals) {
        List<Classroom> fresh = new ArrayList<>();
        for (Classroom orig : originals) {
            fresh.add(new Classroom(orig.getRoomName(), orig.getRows(), orig.getColumns()));
        }
        return fresh;
    }

    @Override
    public String getName() {
        return "Random Shuffle";
    }

    @Override
    public String getDescription() {
        return "Randomizes seating with configurable constraints. Supports room branch assignments.";
    }

    /**
     * Helper class to track seat positions.
     */
    private static class SeatPosition {
        Classroom classroom;
        int row;
        int col;

        SeatPosition(Classroom classroom, int row, int col) {
            this.classroom = classroom;
            this.row = row;
            this.col = col;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof SeatPosition)) return false;
            SeatPosition other = (SeatPosition) obj;
            return row == other.row && col == other.col && 
                   classroom.getRoomName().equals(other.classroom.getRoomName());
        }

        @Override
        public int hashCode() {
            return Objects.hash(classroom.getRoomName(), row, col);
        }
    }
}
