package edu.univ.erp.util;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.Student;
import edu.univ.erp.service.InstructorService.ClassStatistics;

public class PDFExporter {
    private static final Logger logger = Logger.getLogger(PDFExporter.class.getName());
    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font BOLD_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    
    public static boolean exportTranscript(Student student, List<Enrollment> enrollments, 
                                         List<Grade> grades, String filePath) {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
            
            addUniversityHeader(document);
            
            Paragraph title = new Paragraph("ACADEMIC TRANSCRIPT", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
            
            addStudentInfo(document, student);
            
            addGradesTable(document, enrollments, grades);
            
            addFooter(document);
            
            document.close();
            logger.info("PDF transcript exported successfully to: " + filePath);
            return true;
            
        } catch (DocumentException | IOException e) {
            logger.severe("Failed to export PDF transcript: " + e.getMessage());
            return false;
        }
    }
    
    private static void addUniversityHeader(Document document) throws DocumentException {
        Paragraph university = new Paragraph("UNIVERSITY ERP SYSTEM", HEADER_FONT);
        university.setAlignment(Element.ALIGN_CENTER);
        document.add(university);
        
        Paragraph address = new Paragraph("University, New Delhi", NORMAL_FONT);
        address.setAlignment(Element.ALIGN_CENTER);
        address.setSpacingAfter(10);
        document.add(address);
        
        document.add(new Paragraph(" ")); 
    }
    
    private static void addStudentInfo(Document document, Student student) throws DocumentException {
        PdfPTable infoTable = new PdfPTable(4);
        infoTable.setWidthPercentage(100);
        infoTable.setSpacingAfter(15);
        
        addInfoRow(infoTable, "Student ID:", student.getRollNo());
        addInfoRow(infoTable, "Name:", "Student Name"); 
        addInfoRow(infoTable, "Program:", student.getProgram());
        addInfoRow(infoTable, "Year:", String.valueOf(student.getYear()));
        
        document.add(infoTable);
        document.add(new Paragraph(" ")); 
    }
    
    private static void addInfoRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, BOLD_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(valueCell);
    }
    
    private static void addGradesTable(Document document, List<Enrollment> enrollments, 
                                     List<Grade> grades) throws DocumentException {
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setWidths(new float[]{2, 3, 1, 1.5f, 1.5f, 1.5f});
        
        String[] headers = {"Course Code", "Course Title", "Credits", "Semester", "Year", "Grade"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setBackgroundColor(new Color(240, 240, 240));
            table.addCell(cell);
        }
        
        for (Enrollment enrollment : enrollments) {
            Double finalGrade = grades.stream()
                .filter(g -> g.getEnrollmentId() == enrollment.getEnrollmentId())
                .filter(g -> g.getFinalGrade() != null)
                .map(Grade::getFinalGrade)
                .findFirst()
                .orElse(null);
            
            String gradeLetter = convertToLetterGrade(finalGrade);
            
            table.addCell(new Phrase("CS101", NORMAL_FONT));
            table.addCell(new Phrase("Introduction to Programming", NORMAL_FONT));
            table.addCell(new Phrase("3", NORMAL_FONT));
            table.addCell(new Phrase("Fall 2024", NORMAL_FONT));
            table.addCell(new Phrase("2024", NORMAL_FONT));
            
            PdfPCell gradeCell = new PdfPCell(new Phrase(gradeLetter, BOLD_FONT));
            gradeCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(gradeCell);
        }
        
        document.add(table);
    }
    
    private static void addFooter(Document document) throws DocumentException {
        document.add(new Paragraph(" "));
        
        Paragraph footer = new Paragraph(
            "This document is computer-generated and requires no signature.", 
            FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 9)
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);
        
        Paragraph date = new Paragraph(
            "Generated on: " + java.time.LocalDate.now().toString(),
            FontFactory.getFont(FontFactory.HELVETICA, 9)
        );
        date.setAlignment(Element.ALIGN_CENTER);
        document.add(date);
    }
    
    private static String convertToLetterGrade(Double numericGrade) {
        if (numericGrade == null) return "N/A";
        if (numericGrade >= 90) return "A";
        if (numericGrade >= 80) return "B";
        if (numericGrade >= 70) return "C";
        if (numericGrade >= 60) return "D";
        return "F";
    }

    public static boolean exportClassReport(ClassStatistics stats, String filePath) {
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
            
            addUniversityHeader(document);
            
            Paragraph title = new Paragraph("CLASS STATISTICS REPORT", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);
            
            addCourseInfo(document, stats);
            
            addEnrollmentStats(document, stats);
            
            addPerformanceStats(document, stats);
            
            addFooter(document);
            
            document.close();
            logger.info("PDF class report exported successfully to: " + filePath);
            return true;
            
        } catch (DocumentException | IOException e) {
            logger.severe("Failed to export PDF class report: " + e.getMessage());
            return false;
        }
    }
    
    
    
    
    private static void addCourseInfo(Document document, ClassStatistics stats) throws DocumentException {
        Paragraph sectionTitle = new Paragraph("COURSE INFORMATION", HEADER_FONT);
        sectionTitle.setSpacingBefore(10);
        sectionTitle.setSpacingAfter(10);
        document.add(sectionTitle);
        
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[]{1, 3});
        
        addInfoRow(infoTable, "Course Code:", stats.getSection().getCourseCode());
        addInfoRow(infoTable, "Section ID:", String.valueOf(stats.getSection().getSectionId()));
        addInfoRow(infoTable, "Semester:", stats.getSection().getSemester().getDisplayName());
        addInfoRow(infoTable, "Year:", String.valueOf(stats.getSection().getYear()));
        addInfoRow(infoTable, "Room:", stats.getSection().getRoom());
        addInfoRow(infoTable, "Schedule:", stats.getSection().getDay().getDisplayName() + " " + stats.getSection().getTime());
        
        document.add(infoTable);
        document.add(new Paragraph(" ")); 
    }

    private static void addEnrollmentStats(Document document, ClassStatistics stats) throws DocumentException {
        Paragraph sectionTitle = new Paragraph("ENROLLMENT STATISTICS", HEADER_FONT);
        sectionTitle.setSpacingBefore(10);
        sectionTitle.setSpacingAfter(10);
        document.add(sectionTitle);
        
        PdfPTable statsTable = new PdfPTable(2);
        statsTable.setWidthPercentage(100);
        statsTable.setWidths(new float[]{1, 1});
        
        addStatRow(statsTable, "Total Students:", String.valueOf(stats.getTotalStudents()));
        addStatRow(statsTable, "Capacity:", String.valueOf(stats.getSection().getCapacity()));
        addStatRow(statsTable, "Enrollment Rate:", String.format("%.1f%%", stats.getEnrollmentRate()));
        addStatRow(statsTable, "Available Seats:", String.valueOf(stats.getSection().getAvailableSeats()));
        
        document.add(statsTable);
        document.add(new Paragraph(" ")); 
    }

    private static void addPerformanceStats(Document document, ClassStatistics stats) throws DocumentException {
        Paragraph sectionTitle = new Paragraph("PERFORMANCE STATISTICS", HEADER_FONT);
        sectionTitle.setSpacingBefore(10);
        sectionTitle.setSpacingAfter(10);
        document.add(sectionTitle);
        
        PdfPTable perfTable = new PdfPTable(2);
        perfTable.setWidthPercentage(100);
        perfTable.setWidths(new float[]{1, 1});
        
        addStatRow(perfTable, "Average Grade:", String.format("%.2f/100", stats.getAverageGrade()));
        
        // Add grade interpretation
        String interpretation = getGradeInterpretation(stats.getAverageGrade());
        addStatRow(perfTable, "Performance:", interpretation);
        
        document.add(perfTable);
        
        // Add additional notes
        Paragraph notes = new Paragraph("Additional Notes:", BOLD_FONT);
        notes.setSpacingBefore(10);
        document.add(notes);
        
        List<String> noteItems = new ArrayList<>();
        noteItems.add("Based on final grades computed for all enrolled students");
        noteItems.add("Grades are calculated using: Quiz (20%), Midterm (30%), End Semester (50%)");
        noteItems.add("Passing grade: 60/100 and above");
        
        for (String note : noteItems) {
            Paragraph notePara = new Paragraph("• " + note, NORMAL_FONT);
            notePara.setIndentationLeft(10);
            document.add(notePara);
        }
    }

    private static void addStatRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, BOLD_FONT));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPadding(5);
        table.addCell(labelCell);
        
        PdfPCell valueCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPadding(5);
        table.addCell(valueCell);
    }

    private static String getGradeInterpretation(double averageGrade) {
        if (averageGrade >= 90) return "Excellent";
        if (averageGrade >= 80) return "Very Good";
        if (averageGrade >= 70) return "Good";
        if (averageGrade >= 60) return "Satisfactory";
        return "Needs Improvement";
    }
}