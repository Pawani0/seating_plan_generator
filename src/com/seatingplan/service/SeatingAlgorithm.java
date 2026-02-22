package com.seatingplan.service;

import com.seatingplan.model.Classroom;
import com.seatingplan.model.SeatingConfig;
import com.seatingplan.model.SeatingPlan;
import com.seatingplan.model.Student;

import java.util.List;

/**
 * Interface for seating allocation algorithms.
 */
public interface SeatingAlgorithm {

    /**
     * Allocate students to classrooms according to the algorithm's pattern.
     * Uses default configuration.
     *
     * @param students List of students to be seated
     * @param classrooms List of classrooms available
     * @return SeatingPlan with all assignments made
     */
    SeatingPlan allocate(List<Student> students, List<Classroom> classrooms);

    /**
     * Allocate students to classrooms with custom configuration.
     *
     * @param students List of students to be seated
     * @param classrooms List of classrooms available
     * @param config Seating configuration options
     * @return SeatingPlan with all assignments made
     */
    default SeatingPlan allocate(List<Student> students, List<Classroom> classrooms, SeatingConfig config) {
        // Default implementation falls back to basic allocate
        // Subclasses should override to use config
        return allocate(students, classrooms);
    }

    /**
     * Get the display name of this algorithm.
     */
    String getName();

    /**
     * Get a description of how this algorithm works.
     */
    String getDescription();
}
