package com.seatingplan.service;

import com.seatingplan.model.*;
import com.seatingplan.util.SeatTraversal;

import java.util.*;

/**
 * Alternate Year/Semester allocation - interleaves students from different 
 * years/semesters so adjacent seats have different year/semester combinations.
 * Supports room-specific branch assignments.
 */
public class AlternateYearSemAllocator implements SeatingAlgorithm {

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
        plan.setPatternUsed(PatternType.ALTERNATE_YEAR_SEM);

        List<Student> unassigned = new ArrayList<>();

        // Process each classroom
        for (Classroom classroom : freshClassrooms) {
            // Get students eligible for this room
            List<Student> roomStudents = new ArrayList<>();
            for (Student student : students) {
                if (classroom.isBranchAllowed(student.getBranch())) {
                    roomStudents.add(student);
                }
            }
            
            if (roomStudents.isEmpty()) continue;

            // Group students by year-semester combination
            Map<String, List<Student>> yearSemGroups = new LinkedHashMap<>();
            for (Student student : roomStudents) {
                String key = student.getYearSemKey();
                yearSemGroups.computeIfAbsent(key, k -> new ArrayList<>()).add(student);
            }

            // Sort each group by roll number
            for (List<Student> group : yearSemGroups.values()) {
                group.sort(Comparator.comparing(Student::getRollNo));
            }

            // Create interleaved list
            List<Student> interleavedStudents = interleave(yearSemGroups);

            // Get seats in traversal order
            List<Seat> seats = SeatTraversal.getSeatsInOrder(classroom, config);
            
            // Assign with constraint checking
            for (Seat seat : seats) {
                if (interleavedStudents.isEmpty()) break;
                
                Student bestFit = findBestFitStudent(classroom, seat.getRow(), seat.getColumn(),
                                                     interleavedStudents, config);
                if (bestFit != null) {
                    seat.assignStudent(bestFit);
                    interleavedStudents.remove(bestFit);
                }
            }
        }

        // Collect unassigned
        collectUnassignedStudents(freshClassrooms, students, unassigned);
        plan.setUnassignedStudents(unassigned);
        return plan;
    }

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
                unassigned.add(student);
            }
        }
    }

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

    private Student findBestFitStudent(Classroom classroom, int row, int col, 
                                       List<Student> remaining, SeatingConfig config) {
        if (remaining.isEmpty()) return null;

        // Find student with no year/sem conflict
        for (Student candidate : remaining) {
            boolean hasConflict = false;
            
            if (config.isEnforceNoSameYearAdjacent() && 
                NeighborValidator.hasSameYearNeighbor(classroom, row, col, candidate)) {
                hasConflict = true;
            } else if (!NeighborValidator.hasSameYearSemNeighbor(classroom, row, col, candidate)) {
                // Default: check year+sem combo
            } else {
                hasConflict = true;
            }
            
            if (!hasConflict) {
                return candidate;
            }
        }

        // Fallback
        return remaining.get(0);
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
        return "Alternate Year/Semester";
    }

    @Override
    public String getDescription() {
        return "Interleaves students from different years/semesters. Supports room branch assignments.";
    }
}
