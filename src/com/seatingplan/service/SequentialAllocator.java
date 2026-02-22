package com.seatingplan.service;

import com.seatingplan.model.*;
import com.seatingplan.util.SeatTraversal;

import java.util.*;

/**
 * Sequential allocation - fills seats in order across rooms.
 * Supports customizable fill direction, starting corner, and gapping.
 */
public class SequentialAllocator implements SeatingAlgorithm {

    @Override
    public SeatingPlan allocate(List<Student> students, List<Classroom> classrooms) {
        return allocate(students, classrooms, new SeatingConfig());
    }

    @Override
    public SeatingPlan allocate(List<Student> students, List<Classroom> classrooms, SeatingConfig config) {
        // Create fresh classrooms to avoid modifying input
        List<Classroom> freshClassrooms = createFreshClassrooms(classrooms);
        
        // Copy branch assignments
        for (int i = 0; i < classrooms.size(); i++) {
            freshClassrooms.get(i).setAssignedBranches(classrooms.get(i).getAssignedBranches());
        }
        
        SeatingPlan plan = new SeatingPlan(freshClassrooms, new ArrayList<>(students));
        plan.setPatternUsed(PatternType.SEQUENTIAL);

        // Sort students based on config
        List<Student> sortedStudents = sortStudents(new ArrayList<>(students), config);

        // Separate students by room assignments if branch restrictions exist
        Map<Classroom, List<Student>> roomStudentMap = new LinkedHashMap<>();
        List<Student> unassignedByBranch = new ArrayList<>();

        if (hasBranchRestrictions(freshClassrooms)) {
            // Assign students to their designated rooms
            for (Student student : sortedStudents) {
                boolean assigned = false;
                for (Classroom classroom : freshClassrooms) {
                    if (classroom.isBranchAllowed(student.getBranch())) {
                        roomStudentMap.computeIfAbsent(classroom, k -> new ArrayList<>()).add(student);
                        assigned = true;
                        break;
                    }
                }
                if (!assigned) {
                    unassignedByBranch.add(student);
                }
            }
        } else {
            // No restrictions - distribute sequentially
            int studentIndex = 0;
            for (Classroom classroom : freshClassrooms) {
                List<Student> roomStudents = new ArrayList<>();
                int capacity = SeatTraversal.getEffectiveCapacity(classroom, config);
                
                for (int i = 0; i < capacity && studentIndex < sortedStudents.size(); i++) {
                    roomStudents.add(sortedStudents.get(studentIndex++));
                }
                roomStudentMap.put(classroom, roomStudents);
            }
            
            // Any remaining are unassigned
            while (studentIndex < sortedStudents.size()) {
                unassignedByBranch.add(sortedStudents.get(studentIndex++));
            }
        }

        // Now assign students to seats in each room
        for (Classroom classroom : freshClassrooms) {
            List<Student> roomStudents = roomStudentMap.getOrDefault(classroom, new ArrayList<>());
            List<Seat> seats = SeatTraversal.getSeatsInOrder(classroom, config);
            
            int studentIndex = 0;
            for (Seat seat : seats) {
                if (studentIndex >= roomStudents.size()) break;
                seat.assignStudent(roomStudents.get(studentIndex++));
            }
            
            // Any students that couldn't fit
            while (studentIndex < roomStudents.size()) {
                unassignedByBranch.add(roomStudents.get(studentIndex++));
            }
        }

        plan.setUnassignedStudents(unassignedByBranch);
        return plan;
    }

    /**
     * Sort students based on configuration.
     */
    private List<Student> sortStudents(List<Student> students, SeatingConfig config) {
        switch (config.getSortOrder()) {
            case NAME:
                students.sort(Comparator.comparing(Student::getName, String.CASE_INSENSITIVE_ORDER));
                break;
            case BRANCH_THEN_ROLL:
                students.sort(Comparator.comparing(Student::getBranch, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Student::getRollNo));
                break;
            case YEAR_THEN_ROLL:
                students.sort(Comparator.comparingInt(Student::getYear)
                        .thenComparing(Student::getRollNo));
                break;
            case RANDOM:
                Collections.shuffle(students);
                break;
            case ROLL_NUMBER:
            default:
                students.sort(Comparator.comparing(Student::getRollNo));
                break;
        }
        return students;
    }

    /**
     * Check if any classroom has branch restrictions.
     */
    private boolean hasBranchRestrictions(List<Classroom> classrooms) {
        return classrooms.stream().anyMatch(Classroom::hasBranchRestrictions);
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
        return "Sequential";
    }

    @Override
    public String getDescription() {
        return "Fills seats in order across all classrooms. Supports customizable direction, starting corner, and gapping.";
    }
}
