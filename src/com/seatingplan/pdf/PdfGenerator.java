package com.seatingplan.pdf;

import com.seatingplan.model.*;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

import java.io.IOException;
import java.util.*;

/**
 * Generates PDF seating plan documents using Apache PDFBox.
 */
public class PdfGenerator {

    private static final float MARGIN = 50;
    private static final float LINE_HEIGHT = 14;
    private static final float CELL_PADDING = 5;

    /**
     * Generate a PDF document with the seating plan.
     *
     * @param plan The seating plan to export
     * @param outputPath Path to save the PDF file
     * @throws IOException If PDF generation fails
     */
    public static void generatePdf(SeatingPlan plan, String outputPath) throws IOException {
        try (PDDocument document = new PDDocument()) {

            // Generate room layout pages
            for (Classroom classroom : plan.getClassrooms()) {
                generateRoomPage(document, classroom, plan);
            }

            // Generate summary page
            generateSummaryPage(document, plan);

            document.save(outputPath);
        }
    }

    /**
     * Generate a page with the classroom seating grid.
     */
    private static void generateRoomPage(PDDocument document, Classroom classroom, SeatingPlan plan) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);

        float pageWidth = page.getMediaBox().getWidth();
        float pageHeight = page.getMediaBox().getHeight();

        try (PDPageContentStream content = new PDPageContentStream(document, page)) {

            float yPosition = pageHeight - MARGIN;

            // Title
            content.beginText();
            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 16);
            content.newLineAtOffset(MARGIN, yPosition);
            content.showText("SEATING PLAN - " + classroom.getRoomName().toUpperCase());
            content.endText();
            yPosition -= 25;

            // Exam info
            content.beginText();
            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            content.newLineAtOffset(MARGIN, yPosition);
            String examInfo = "";
            if (plan.getExamName() != null && !plan.getExamName().isEmpty()) {
                examInfo += "Exam: " + plan.getExamName() + "   ";
            }
            if (plan.getExamDate() != null && !plan.getExamDate().isEmpty()) {
                examInfo += "Date: " + plan.getExamDate();
            }
            if (!examInfo.isEmpty()) {
                content.showText(examInfo);
            }
            content.endText();
            yPosition -= 15;

            // Room info
            content.beginText();
            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
            content.newLineAtOffset(MARGIN, yPosition);
            String roomInfo = "Pattern: " + plan.getPatternUsed().getDisplayName() + 
                           "   |   Capacity: " + classroom.getCapacity() + 
                           "   |   Occupied: " + classroom.getOccupiedCount();
            content.showText(roomInfo);
            content.endText();
            yPosition -= 15;
            
            // Branch info if restricted
            if (classroom.hasBranchRestrictions()) {
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
                content.newLineAtOffset(MARGIN, yPosition);
                content.showText("Branches: " + classroom.getBranchSummary());
                content.endText();
                yPosition -= 15;
            }
            yPosition -= 15;

            // Calculate cell dimensions
            float availableWidth = pageWidth - (2 * MARGIN);
            float availableHeight = yPosition - MARGIN - 100; // Leave space for footer

            int cols = classroom.getColumns();
            int rows = classroom.getRows();

            float cellWidth = Math.min(availableWidth / cols, 80);
            float cellHeight = Math.min(availableHeight / rows, 50);

            float tableWidth = cellWidth * cols;
            float tableHeight = cellHeight * rows;
            float startX = MARGIN + (availableWidth - tableWidth) / 2;
            float startY = yPosition;

            // Draw column headers
            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 8);
            for (int c = 0; c < cols; c++) {
                float x = startX + (c * cellWidth) + (cellWidth / 2) - 5;
                content.beginText();
                content.newLineAtOffset(x, startY + 5);
                content.showText("C" + (c + 1));
                content.endText();
            }
            startY -= 15;

            // Draw grid
            for (int r = 0; r < rows; r++) {
                float y = startY - (r * cellHeight);

                // Row header
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 8);
                content.newLineAtOffset(startX - 20, y - (cellHeight / 2) - 3);
                content.showText("R" + (r + 1));
                content.endText();

                for (int c = 0; c < cols; c++) {
                    float x = startX + (c * cellWidth);
                    Seat seat = classroom.getSeat(r, c);

                    // Draw cell border
                    content.setStrokingColor(0.5f, 0.5f, 0.5f);
                    content.addRect(x, y - cellHeight, cellWidth, cellHeight);
                    content.stroke();

                    // Draw cell content
                    if (seat != null && seat.isOccupied()) {
                        Student student = seat.getAssignedStudent();

                        // Fill with light color based on branch
                        float[] color = getBranchRGB(student.getBranch());
                        content.setNonStrokingColor(color[0], color[1], color[2]);
                        content.addRect(x + 1, y - cellHeight + 1, cellWidth - 2, cellHeight - 2);
                        content.fill();
                        content.setNonStrokingColor(0, 0, 0);

                        // Roll number
                        content.beginText();
                        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 8);
                        content.newLineAtOffset(x + CELL_PADDING, y - 12);
                        content.showText(truncate(student.getRollNo(), 12));
                        content.endText();

                        // Name
                        content.beginText();
                        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 7);
                        content.newLineAtOffset(x + CELL_PADDING, y - 22);
                        content.showText(truncate(student.getName(), 12));
                        content.endText();

                        // Branch
                        content.beginText();
                        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 6);
                        content.newLineAtOffset(x + CELL_PADDING, y - 32);
                        content.showText(student.getBranch() + " Y" + student.getYear());
                        content.endText();
                    } else {
                        // Empty seat
                        content.setNonStrokingColor(0.95f, 0.95f, 0.95f);
                        content.addRect(x + 1, y - cellHeight + 1, cellWidth - 2, cellHeight - 2);
                        content.fill();
                        content.setNonStrokingColor(0.6f, 0.6f, 0.6f);

                        content.beginText();
                        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 8);
                        content.newLineAtOffset(x + cellWidth/2 - 10, y - cellHeight/2 - 3);
                        content.showText("Empty");
                        content.endText();

                        content.setNonStrokingColor(0, 0, 0);
                    }
                }
            }

            // Legend
            float legendY = startY - (rows * cellHeight) - 30;
            content.beginText();
            content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 9);
            content.newLineAtOffset(MARGIN, legendY);
            content.showText("Legend (by Branch):");
            content.endText();

            // Draw branch color legend
            Set<String> branches = new HashSet<>();
            for (Student s : plan.getStudents()) {
                branches.add(s.getBranch());
            }

            float legendX = MARGIN + 100;
            for (String branch : branches) {
                float[] color = getBranchRGB(branch);
                content.setNonStrokingColor(color[0], color[1], color[2]);
                content.addRect(legendX, legendY - 3, 15, 12);
                content.fill();
                content.setNonStrokingColor(0, 0, 0);

                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 8);
                content.newLineAtOffset(legendX + 20, legendY);
                content.showText(branch);
                content.endText();

                legendX += 60;
            }
        }
    }

    /**
     * Generate a summary page with tabular listing.
     */
    private static void generateSummaryPage(PDDocument document, SeatingPlan plan) throws IOException {
        List<SeatingPlan.SeatAssignment> assignments = plan.getAllAssignments();

        // May need multiple pages for large lists
        int entriesPerPage = 40;
        int totalPages = (int) Math.ceil((double) assignments.size() / entriesPerPage);

        for (int pageNum = 0; pageNum < totalPages; pageNum++) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();

            try (PDPageContentStream content = new PDPageContentStream(document, page)) {

                float yPosition = pageHeight - MARGIN;

                // Title
                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 14);
                content.newLineAtOffset(MARGIN, yPosition);
                content.showText("SEATING PLAN SUMMARY" + (totalPages > 1 ? " (Page " + (pageNum + 1) + "/" + totalPages + ")" : ""));
                content.endText();
                yPosition -= 25;

                // Exam info
                if (pageNum == 0) {
                    content.beginText();
                    content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                    content.newLineAtOffset(MARGIN, yPosition);
                    String info = "Pattern: " + plan.getPatternUsed().getDisplayName();
                    if (plan.getExamName() != null && !plan.getExamName().isEmpty()) {
                        info += "  |  Exam: " + plan.getExamName();
                    }
                    if (plan.getExamDate() != null && !plan.getExamDate().isEmpty()) {
                        info += "  |  Date: " + plan.getExamDate();
                    }
                    content.showText(info);
                    content.endText();
                    yPosition -= 15;

                    content.beginText();
                    content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 10);
                    content.newLineAtOffset(MARGIN, yPosition);
                    content.showText("Total Students: " + plan.getStudents().size() + 
                                   "  |  Seated: " + plan.getTotalOccupied() +
                                   "  |  Classrooms: " + plan.getClassrooms().size());
                    content.endText();
                    yPosition -= 30;
                }

                // Table headers
                float[] colWidths = {80, 60, 150, 60, 50, 50};
                String[] headers = {"Room", "Seat", "Name", "Roll No", "Branch", "Year"};

                float tableX = MARGIN;

                // Draw header background
                content.setNonStrokingColor(0.2f, 0.4f, 0.8f);
                content.addRect(tableX, yPosition - LINE_HEIGHT, 
                               colWidths[0] + colWidths[1] + colWidths[2] + colWidths[3] + colWidths[4] + colWidths[5], 
                               LINE_HEIGHT + 2);
                content.fill();
                content.setNonStrokingColor(1, 1, 1);

                content.beginText();
                content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 9);
                float headerX = tableX + 3;
                content.newLineAtOffset(headerX, yPosition - 10);
                for (int i = 0; i < headers.length; i++) {
                    content.showText(headers[i]);
                    if (i < headers.length - 1) {
                        content.newLineAtOffset(colWidths[i], 0);
                    }
                }
                content.endText();
                yPosition -= LINE_HEIGHT + 5;

                content.setNonStrokingColor(0, 0, 0);

                // Table rows
                int startIdx = pageNum * entriesPerPage;
                int endIdx = Math.min(startIdx + entriesPerPage, assignments.size());

                for (int i = startIdx; i < endIdx; i++) {
                    SeatingPlan.SeatAssignment assignment = assignments.get(i);
                    Student student = assignment.getStudent();

                    // Alternate row colors
                    if ((i - startIdx) % 2 == 0) {
                        content.setNonStrokingColor(0.95f, 0.95f, 0.95f);
                        content.addRect(tableX, yPosition - LINE_HEIGHT, 
                                       colWidths[0] + colWidths[1] + colWidths[2] + colWidths[3] + colWidths[4] + colWidths[5], 
                                       LINE_HEIGHT);
                        content.fill();
                        content.setNonStrokingColor(0, 0, 0);
                    }

                    content.beginText();
                    content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 8);
                    float rowX = tableX + 3;
                    content.newLineAtOffset(rowX, yPosition - 10);

                    content.showText(truncate(assignment.getRoomName(), 12));
                    content.newLineAtOffset(colWidths[0], 0);

                    content.showText(assignment.getSeatLabel());
                    content.newLineAtOffset(colWidths[1], 0);

                    content.showText(truncate(student.getName(), 22));
                    content.newLineAtOffset(colWidths[2], 0);

                    content.showText(truncate(student.getRollNo(), 10));
                    content.newLineAtOffset(colWidths[3], 0);

                    content.showText(student.getBranch());
                    content.newLineAtOffset(colWidths[4], 0);

                    content.showText("Y" + student.getYear() + "S" + student.getSemester());

                    content.endText();
                    yPosition -= LINE_HEIGHT;
                }

                // Statistics on first page
                if (pageNum == 0 && yPosition > MARGIN + 100) {
                    yPosition -= 30;
                    content.beginText();
                    content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD), 10);
                    content.newLineAtOffset(MARGIN, yPosition);
                    content.showText("Room-wise Distribution:");
                    content.endText();
                    yPosition -= 15;

                    for (Classroom room : plan.getClassrooms()) {
                        content.beginText();
                        content.setFont(new PDType1Font(Standard14Fonts.FontName.HELVETICA), 9);
                        content.newLineAtOffset(MARGIN + 20, yPosition);
                        content.showText(room.getRoomName() + ": " + room.getOccupiedCount() + " / " + 
                                        room.getCapacity() + " seats occupied");
                        content.endText();
                        yPosition -= LINE_HEIGHT;
                    }
                }
            }
        }
    }

    /**
     * Get RGB color for a branch (consistent color per branch).
     */
    private static float[] getBranchRGB(String branch) {
        int hash = Math.abs(branch.hashCode());
        float[][] colors = {
            {1.0f, 0.8f, 0.8f},    // Light red
            {0.8f, 1.0f, 0.8f},    // Light green
            {0.8f, 0.8f, 1.0f},    // Light blue
            {1.0f, 1.0f, 0.8f},    // Light yellow
            {1.0f, 0.8f, 1.0f},    // Light magenta
            {0.8f, 1.0f, 1.0f},    // Light cyan
            {1.0f, 0.9f, 0.8f},    // Light orange
            {0.9f, 0.8f, 1.0f},    // Light purple
        };
        return colors[hash % colors.length];
    }

    /**
     * Truncate string to max length.
     */
    private static String truncate(String str, int maxLen) {
        if (str == null) return "";
        if (str.length() <= maxLen) return str;
        return str.substring(0, maxLen - 2) + "..";
    }
}
