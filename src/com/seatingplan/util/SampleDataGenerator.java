package com.seatingplan.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Utility to generate a sample student Excel file for testing.
 * Run this after setting up the libraries.
 */
public class SampleDataGenerator {

    private static final String[] BRANCHES = {"CSE", "ECE", "ME", "EE", "CE"};
    private static final String[] FIRST_NAMES = {
        "Aarav", "Vivaan", "Aditya", "Vihaan", "Arjun", "Sai", "Reyansh", "Ayaan", "Krishna", "Ishaan",
        "Ananya", "Diya", "Priya", "Kavya", "Saanvi", "Aanya", "Myra", "Aadhya", "Pari", "Riya",
        "Rahul", "Amit", "Prateek", "Vikram", "Rohan", "Karan", "Nikhil", "Sanjay", "Deepak", "Rajesh",
        "Sneha", "Pooja", "Neha", "Meera", "Shreya", "Tanvi", "Kritika", "Nidhi", "Swati", "Ankita"
    };
    private static final String[] LAST_NAMES = {
        "Sharma", "Verma", "Gupta", "Singh", "Kumar", "Patel", "Reddy", "Rao", "Nair", "Menon",
        "Iyer", "Pillai", "Joshi", "Desai", "Shah", "Mehta", "Agarwal", "Mishra", "Pandey", "Saxena"
    };

    public static void main(String[] args) {
        String outputPath = "resources/sample_students.xlsx";
        int numberOfStudents = 60;

        if (args.length > 0) {
            outputPath = args[0];
        }
        if (args.length > 1) {
            numberOfStudents = Integer.parseInt(args[1]);
        }

        try {
            generateSampleExcel(outputPath, numberOfStudents);
            System.out.println("Sample Excel file created: " + outputPath);
            System.out.println("Students generated: " + numberOfStudents);
        } catch (IOException e) {
            System.err.println("Error creating file: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void generateSampleExcel(String outputPath, int count) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Students");

            // Create header style
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] headers = {"Name", "Roll No", "Year", "Semester", "Branch"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Generate student data
            java.util.Random random = new java.util.Random(42); // Fixed seed for reproducibility

            for (int i = 0; i < count; i++) {
                Row row = sheet.createRow(i + 1);

                // Random name
                String firstName = FIRST_NAMES[random.nextInt(FIRST_NAMES.length)];
                String lastName = LAST_NAMES[random.nextInt(LAST_NAMES.length)];
                row.createCell(0).setCellValue(firstName + " " + lastName);

                // Branch
                String branch = BRANCHES[i % BRANCHES.length]; // Distribute evenly

                // Year (1-4) and corresponding roll number prefix
                int year = (i / 15) % 4 + 1; // Groups of 15 students per year
                int enrollmentYear = 2024 - year + 1;

                // Roll number format: BRANCH + YEAR + SEQUENCE
                String rollNo = branch + enrollmentYear + String.format("%03d", (i % 100) + 1);
                row.createCell(1).setCellValue(rollNo);

                row.createCell(2).setCellValue(year);

                // Semester (odd semester of that year)
                int semester = (year * 2) - 1;
                row.createCell(3).setCellValue(semester);

                row.createCell(4).setCellValue(branch);
            }

            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            // Write to file
            try (FileOutputStream fos = new FileOutputStream(outputPath)) {
                workbook.write(fos);
            }
        }
    }
}
