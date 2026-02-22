package com.seatingplan.service;

import com.seatingplan.model.*;
import com.seatingplan.util.SeatTraversal;

import java.util.*;

/**
 * Alternate Branch allocation - interleaves students from different branches
 * so adjacent seats have different branches wherever possible.
 * Supports room-specific branch assignments (2-3 branches per room).
 */
public class AlternateBranchAllocator implements SeatingAlgorithm {

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
        plan.setPatternUsed(PatternType.ALTERNATE_BRANCH);

        List<Student> unassigned = new ArrayList<>();

        // Process each classroom
        for (Classroom classroom : freshClassrooms) {
            // Get students for this room based on branch assignments
            List<Student> roomStudents = getStudentsForRoom(students, classroom, unassigned);
            
            if (roomStudents.isEmpty()) continue;

            // Group students by branch
            Map<String, List<Student>> branchGroups = new LinkedHashMap<>();
            for (Student student : roomStudents) {
                branchGroups.computeIfAbsent(student.getBranch(), k -> new ArrayList<>()).add(student);
            }

            // Sort each group by roll number
            for (List<Student> group : branchGroups.values()) {
                group.sort(Comparator.comparing(Student::getRollNo));
            }

            // Create interleaved list
            List<Student> interleavedStudents = interleave(branchGroups);

            // Get seats in traversal order
            List<Seat> seats = SeatTraversal.getSeatsInOrder(classroom, config);
            
            // Assign with neighbor checking
            assignWithNeighborCheck(classroom, seats, interleavedStudents, config);
        }

        // Collect any students not assigned to rooms
        collectUnassignedStudents(freshClassrooms, students, unassigned);
        plan.setUnassignedStudents(unassigned);

        return plan;
    }

    /**
     * Get students eligible for a specific room based on branch restrictions.
     */
    private List<Student> getStudentsForRoom(List<Student> allStudents, Classroom classroom, 
                                             List<Student> globalUnassigned) {
        List<Student> eligible = new ArrayList<>();
        
        for (Student student : allStudents) {
            if (classroom.isBranchAllowed(student.getBranch())) {
                eligible.add(student);
            }
        }
        
        return eligible;
    }

    /**
     * Assign students to seats with neighbor checking.
     */
    private void assignWithNeighborCheck(Classroom classroom, List<Seat> seats, 
                                         List<Student> students, SeatingConfig config) {
        List<Student> remaining = new ArrayList<>(students);
        
        for (Seat seat : seats) {
            if (remaining.isEmpty()) break;
            
            // Find best fit student (no same branch neighbor)
            Student bestFit = findBestFitStudent(classroom, seat.getRow(), seat.getColumn(), 
                                                 remaining, config);
            if (bestFit != null) {
                seat.assignStudent(bestFit);
                remaining.remove(bestFit);
            }
        }
    }

    /**
     * Find the best fitting student considering neighbor constraints.
     */
    private Student findBestFitStudent(Classroom classroom, int row, int col, 
                                       List<Student> remaining, SeatingConfig config) {
        if (remaining.isEmpty()) return null;

        // First pass: find student with no conflicts
        for (Student candidate : remaining) {
            boolean hasConflict = false;
            
            if (config.isEnforceNoSameBranchAdjacent() && 
                NeighborValidator.hasSameBranchNeighbor(classroom, row, col, candidate)) {
                hasConflict = true;
            }
            
            if (config.isEnforceNoSameYearAdjacent() && 
                NeighborValidator.hasSameYearNeighbor(classroom, row, col, candidate)) {
                hasConflict = true;
            }
            
            if (!hasConflict) {
                return candidate;
            }
        }

        // Fallback: return first student if no conflict-free option
        return remaining.get(0);
    }

    /**
     * Collect students that weren't assigned to any room.
     */
    private void collectUnassignedStudents(List<Classroom> classrooms, List<Student> allStudents,
                                          List<Student> unassigned) {
        Set<String> assignedRollNos = new HashSet<>();
        
        for (Classroom classroom : classrooms) {
            for (int r = 0; r < classroom.getRows(); r++) {
                for (int c = 0; c < classroom.getColumns(); c++) {
                    Seat seat = classroom.getSeat(r, c);
                    if (seat != null && seat.isOccupied()) {
                        assignedRollNos.add(seat.getAssignedStudent().getRollNo());
                    }
                }
            }
        }
        
        for (Student student : allStudents) {
            if (!assignedRollNos.contains(student.getRollNo())) {
                if (!unassigned.contains(student)) {
                    unassigned.add(student);
                }
            }
        }
    }

    /**
     * Interleave students from different groups in round-robin fashion.
     */
    private List<Student> interleave(Map<String, List<Student>> groups) {
        List<Student> result = new ArrayList<>();
        List<Iterator<Student>> iterators = new ArrayList<>();

        for (List<Student> group : groups.values()) {
            iterators.add(group.iterator());
        }

        boolean hasMore = true;
        while (hasMore) {
            hasMore = false;
            for (Iterator<Student> it : iterators) {
                if (it.hasNext()) {
                    result.add(it.next());
                    hasMore = true;
                }
            }
        }

        return result;
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
        return "Alternate Branch";
    }

    @Override
    public String getDescription() {
        return "Interleaves students from different branches. Supports room-specific branch assignments.";
    }
}
