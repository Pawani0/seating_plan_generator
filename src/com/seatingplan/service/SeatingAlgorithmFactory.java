package com.seatingplan.service;

import com.seatingplan.model.PatternType;

/**
 * Factory class to create appropriate SeatingAlgorithm based on PatternType.
 */
public class SeatingAlgorithmFactory {

    /**
     * Get the appropriate algorithm for the given pattern type.
     */
    public static SeatingAlgorithm getAlgorithm(PatternType patternType) {
        switch (patternType) {
            case SEQUENTIAL:
                return new SequentialAllocator();
            case ALTERNATE_BRANCH:
                return new AlternateBranchAllocator();
            case ALTERNATE_YEAR_SEM:
                return new AlternateYearSemAllocator();
            case RANDOM_SHUFFLE:
                return new RandomShuffleAllocator();
            default:
                throw new IllegalArgumentException("Unknown pattern type: " + patternType);
        }
    }
}
