package com.seatingplan.util;

import com.seatingplan.model.Classroom;
import com.seatingplan.model.Seat;
import com.seatingplan.model.SeatingConfig;
import com.seatingplan.model.SeatingConfig.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for traversing seats in different orders based on configuration.
 */
public class SeatTraversal {

    /**
     * Get seats in the order specified by the configuration.
     * 
     * @param classroom The classroom to traverse
     * @param config The seating configuration
     * @return List of seats in traversal order
     */
    public static List<Seat> getSeatsInOrder(Classroom classroom, SeatingConfig config) {
        List<Seat> seats = new ArrayList<>();
        
        int rows = classroom.getRows();
        int cols = classroom.getColumns();
        
        FillDirection direction = config.getFillDirection();
        StartCorner corner = config.getStartCorner();
        SeatGapping gapping = config.getSeatGapping();
        
        // Determine start positions and increments based on corner
        int rowStart, rowEnd, rowInc;
        int colStart, colEnd, colInc;
        
        switch (corner) {
            case TOP_RIGHT:
                rowStart = 0; rowEnd = rows; rowInc = 1;
                colStart = cols - 1; colEnd = -1; colInc = -1;
                break;
            case BOTTOM_LEFT:
                rowStart = rows - 1; rowEnd = -1; rowInc = -1;
                colStart = 0; colEnd = cols; colInc = 1;
                break;
            case BOTTOM_RIGHT:
                rowStart = rows - 1; rowEnd = -1; rowInc = -1;
                colStart = cols - 1; colEnd = -1; colInc = -1;
                break;
            case TOP_LEFT:
            default:
                rowStart = 0; rowEnd = rows; rowInc = 1;
                colStart = 0; colEnd = cols; colInc = 1;
                break;
        }
        
        switch (direction) {
            case COLUMN_FIRST:
                seats = traverseColumnFirst(classroom, rowStart, rowEnd, rowInc, 
                                           colStart, colEnd, colInc, gapping);
                break;
            case ZIGZAG_ROW:
                seats = traverseZigzagRow(classroom, rowStart, rowEnd, rowInc, 
                                         colStart, colEnd, colInc, gapping);
                break;
            case ZIGZAG_COLUMN:
                seats = traverseZigzagColumn(classroom, rowStart, rowEnd, rowInc, 
                                            colStart, colEnd, colInc, gapping);
                break;
            case ROW_FIRST:
            default:
                seats = traverseRowFirst(classroom, rowStart, rowEnd, rowInc, 
                                        colStart, colEnd, colInc, gapping);
                break;
        }
        
        return seats;
    }

    /**
     * Row-first traversal (fill row completely, then move to next).
     */
    private static List<Seat> traverseRowFirst(Classroom classroom, 
            int rowStart, int rowEnd, int rowInc,
            int colStart, int colEnd, int colInc,
            SeatGapping gapping) {
        
        List<Seat> seats = new ArrayList<>();
        int rowCount = 0;
        
        for (int r = rowStart; condition(r, rowEnd, rowInc); r += rowInc) {
            // Skip rows based on gapping
            if (gapping == SeatGapping.ALTERNATE_ROWS && rowCount % 2 == 1) {
                rowCount++;
                continue;
            }
            
            int colCount = 0;
            for (int c = colStart; condition(c, colEnd, colInc); c += colInc) {
                // Skip seats based on gapping
                if (shouldSkipSeat(gapping, rowCount, colCount)) {
                    colCount++;
                    continue;
                }
                
                Seat seat = classroom.getSeat(r, c);
                if (seat != null) {
                    seats.add(seat);
                }
                colCount++;
            }
            rowCount++;
        }
        
        return seats;
    }

    /**
     * Column-first traversal (fill column completely, then move to next).
     */
    private static List<Seat> traverseColumnFirst(Classroom classroom, 
            int rowStart, int rowEnd, int rowInc,
            int colStart, int colEnd, int colInc,
            SeatGapping gapping) {
        
        List<Seat> seats = new ArrayList<>();
        int colCount = 0;
        
        for (int c = colStart; condition(c, colEnd, colInc); c += colInc) {
            // Skip columns based on gapping
            if (gapping == SeatGapping.ALTERNATE_ROWS && colCount % 2 == 1) {
                colCount++;
                continue;
            }
            
            int rowCount = 0;
            for (int r = rowStart; condition(r, rowEnd, rowInc); r += rowInc) {
                // Skip seats based on gapping
                if (shouldSkipSeat(gapping, rowCount, colCount)) {
                    rowCount++;
                    continue;
                }
                
                Seat seat = classroom.getSeat(r, c);
                if (seat != null) {
                    seats.add(seat);
                }
                rowCount++;
            }
            colCount++;
        }
        
        return seats;
    }

    /**
     * Zigzag row traversal (alternate direction each row).
     */
    private static List<Seat> traverseZigzagRow(Classroom classroom, 
            int rowStart, int rowEnd, int rowInc,
            int colStart, int colEnd, int colInc,
            SeatGapping gapping) {
        
        List<Seat> seats = new ArrayList<>();
        int rowCount = 0;
        boolean reverseCol = false;
        
        for (int r = rowStart; condition(r, rowEnd, rowInc); r += rowInc) {
            // Skip rows based on gapping
            if (gapping == SeatGapping.ALTERNATE_ROWS && rowCount % 2 == 1) {
                rowCount++;
                reverseCol = !reverseCol;
                continue;
            }
            
            int colCount = 0;
            int actualColStart = reverseCol ? (colEnd - colInc) : colStart;
            int actualColEnd = reverseCol ? (colStart - colInc) : colEnd;
            int actualColInc = reverseCol ? -colInc : colInc;
            
            for (int c = actualColStart; condition(c, actualColEnd, actualColInc); c += actualColInc) {
                // Skip seats based on gapping
                if (shouldSkipSeat(gapping, rowCount, colCount)) {
                    colCount++;
                    continue;
                }
                
                Seat seat = classroom.getSeat(r, c);
                if (seat != null) {
                    seats.add(seat);
                }
                colCount++;
            }
            rowCount++;
            reverseCol = !reverseCol;
        }
        
        return seats;
    }

    /**
     * Zigzag column traversal (alternate direction each column).
     */
    private static List<Seat> traverseZigzagColumn(Classroom classroom, 
            int rowStart, int rowEnd, int rowInc,
            int colStart, int colEnd, int colInc,
            SeatGapping gapping) {
        
        List<Seat> seats = new ArrayList<>();
        int colCount = 0;
        boolean reverseRow = false;
        
        for (int c = colStart; condition(c, colEnd, colInc); c += colInc) {
            int rowCount = 0;
            int actualRowStart = reverseRow ? (rowEnd - rowInc) : rowStart;
            int actualRowEnd = reverseRow ? (rowStart - rowInc) : rowEnd;
            int actualRowInc = reverseRow ? -rowInc : rowInc;
            
            for (int r = actualRowStart; condition(r, actualRowEnd, actualRowInc); r += actualRowInc) {
                // Skip seats based on gapping
                if (shouldSkipSeat(gapping, rowCount, colCount)) {
                    rowCount++;
                    continue;
                }
                
                Seat seat = classroom.getSeat(r, c);
                if (seat != null) {
                    seats.add(seat);
                }
                rowCount++;
            }
            colCount++;
            reverseRow = !reverseRow;
        }
        
        return seats;
    }

    /**
     * Helper to check loop condition based on increment direction.
     */
    private static boolean condition(int current, int end, int inc) {
        if (inc > 0) {
            return current < end;
        } else {
            return current > end;
        }
    }

    /**
     * Check if a seat should be skipped based on gapping configuration.
     */
    private static boolean shouldSkipSeat(SeatGapping gapping, int rowIndex, int colIndex) {
        switch (gapping) {
            case ALTERNATE_SEATS:
                return colIndex % 2 == 1;
            case CHECKERBOARD:
                return (rowIndex + colIndex) % 2 == 1;
            case ALTERNATE_ROWS:
            case NO_GAP:
            default:
                return false;
        }
    }

    /**
     * Get the effective capacity considering gapping.
     */
    public static int getEffectiveCapacity(Classroom classroom, SeatingConfig config) {
        return getSeatsInOrder(classroom, config).size();
    }

    /**
     * Get total effective capacity for all classrooms.
     */
    public static int getTotalEffectiveCapacity(List<Classroom> classrooms, SeatingConfig config) {
        int total = 0;
        for (Classroom classroom : classrooms) {
            total += getEffectiveCapacity(classroom, config);
        }
        return total;
    }
}
