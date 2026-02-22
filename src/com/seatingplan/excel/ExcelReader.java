package com.seatingplan.excel;

import com.seatingplan.model.Student;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Reads student data from Excel (.xlsx) files using Apache POI.
 */
public class ExcelReader {

    // Expected column headers (case-insensitive matching)
    private static final String[] EXPECTED_HEADERS = {"Name", "Roll No", "Year", "Semester", "Branch"};

    /**
     * Read students from an Excel file.
     * Expected columns: Name, Roll No, Year, Semester, Branch
     *
     * @param filePath Path to the Excel file
     * @return List of students sorted by roll number
     * @throws IOException If file cannot be read
     * @throws ExcelParseException If file format is invalid
     */
    public static List<Student> readStudents(String filePath) throws IOException, ExcelParseException {
        return readStudents(new File(filePath));
    }

    /**
     * Read students from an Excel file.
     *
     * @param file The Excel file
     * @return List of students sorted by roll number
     * @throws IOException If file cannot be read
     * @throws ExcelParseException If file format is invalid
     */
    public static List<Student> readStudents(File file) throws IOException, ExcelParseException {
        List<Student> students = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        if (!file.exists()) {
            throw new IOException("File not found: " + file.getAbsolutePath());
        }

        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                throw new ExcelParseException("Excel file has no sheets");
            }

            // Find column indices from header row
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                throw new ExcelParseException("Excel file has no header row");
            }

            int[] columnIndices = findColumnIndices(headerRow);

            // Read data rows
            int totalRows = sheet.getLastRowNum();
            for (int rowNum = 1; rowNum <= totalRows; rowNum++) {
                Row row = sheet.getRow(rowNum);
                if (row == null || isRowEmpty(row)) {
                    continue;
                }

                try {
                    Student student = parseStudentRow(row, columnIndices, rowNum + 1);
                    if (student != null) {
                        students.add(student);
                    }
                } catch (Exception e) {
                    errors.add("Row " + (rowNum + 1) + ": " + e.getMessage());
                }
            }
        }

        if (!errors.isEmpty() && students.isEmpty()) {
            throw new ExcelParseException("Failed to parse any students. Errors:\n" + String.join("\n", errors));
        }

        // Sort by roll number
        students.sort(Comparator.comparing(Student::getRollNo));

        return students;
    }

    /**
     * Find column indices for expected headers.
     */
    private static int[] findColumnIndices(Row headerRow) throws ExcelParseException {
        int[] indices = {-1, -1, -1, -1, -1}; // Name, RollNo, Year, Semester, Branch

        for (int col = 0; col < headerRow.getLastCellNum(); col++) {
            Cell cell = headerRow.getCell(col);
            if (cell == null) continue;

            String header = getCellStringValue(cell).trim().toLowerCase();

            if (header.contains("name") && !header.contains("roll")) {
                indices[0] = col;
            } else if (header.contains("roll") || header.contains("rollno") || header.contains("roll_no")) {
                indices[1] = col;
            } else if (header.contains("year") || header.equals("yr")) {
                indices[2] = col;
            } else if (header.contains("sem") || header.contains("semester")) {
                indices[3] = col;
            } else if (header.contains("branch") || header.contains("dept") || header.contains("department")) {
                indices[4] = col;
            }
        }

        // Validate all required columns found
        StringBuilder missing = new StringBuilder();
        if (indices[0] == -1) missing.append("Name, ");
        if (indices[1] == -1) missing.append("Roll No, ");
        if (indices[2] == -1) missing.append("Year, ");
        if (indices[3] == -1) missing.append("Semester, ");
        if (indices[4] == -1) missing.append("Branch, ");

        if (missing.length() > 0) {
            throw new ExcelParseException("Missing required columns: " + 
                missing.substring(0, missing.length() - 2) +
                "\nExpected headers: Name, Roll No, Year, Semester, Branch");
        }

        return indices;
    }

    /**
     * Parse a single row into a Student object.
     */
    private static Student parseStudentRow(Row row, int[] columnIndices, int rowNumber) {
        String name = getCellStringValue(row.getCell(columnIndices[0])).trim();
        String rollNo = getCellStringValue(row.getCell(columnIndices[1])).trim();
        String yearStr = getCellStringValue(row.getCell(columnIndices[2])).trim();
        String semStr = getCellStringValue(row.getCell(columnIndices[3])).trim();
        String branch = getCellStringValue(row.getCell(columnIndices[4])).trim();

        // Skip if essential fields are empty
        if (rollNo.isEmpty()) {
            return null;
        }

        // Parse numeric fields
        int year;
        int semester;

        try {
            year = parseIntValue(yearStr);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid year value: " + yearStr);
        }

        try {
            semester = parseIntValue(semStr);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Invalid semester value: " + semStr);
        }

        return new Student(name, rollNo, year, semester, branch.toUpperCase());
    }

    /**
     * Convert cell to string value regardless of cell type.
     */
    private static String getCellStringValue(Cell cell) {
        if (cell == null) {
            return "";
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                // Handle both integer and decimal numbers
                double numValue = cell.getNumericCellValue();
                if (numValue == Math.floor(numValue)) {
                    return String.valueOf((long) numValue);
                }
                return String.valueOf(numValue);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue();
                } catch (Exception e) {
                    try {
                        return String.valueOf(cell.getNumericCellValue());
                    } catch (Exception e2) {
                        return "";
                    }
                }
            case BLANK:
            default:
                return "";
        }
    }

    /**
     * Parse integer from string, handling decimal formats.
     */
    private static int parseIntValue(String value) {
        if (value == null || value.isEmpty()) {
            return 0;
        }
        // Handle decimal format (e.g., "3.0")
        if (value.contains(".")) {
            return (int) Double.parseDouble(value);
        }
        return Integer.parseInt(value);
    }

    /**
     * Check if a row is completely empty.
     */
    private static boolean isRowEmpty(Row row) {
        for (int c = 0; c < row.getLastCellNum(); c++) {
            Cell cell = row.getCell(c);
            if (cell != null && cell.getCellType() != CellType.BLANK) {
                String value = getCellStringValue(cell).trim();
                if (!value.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Custom exception for Excel parsing errors.
     */
    public static class ExcelParseException extends Exception {
        public ExcelParseException(String message) {
            super(message);
        }
    }
}
