package edu.univ.erp.util;

import com.opencsv.CSVWriter;
import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.GradeComponent;
import edu.univ.erp.domain.Enrollment;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

public class CSVExporter {
    private static final Logger logger = Logger.getLogger(CSVExporter.class.getName());
    
    public static boolean exportTranscript(List<Enrollment> enrollments, List<Grade> grades, String filePath) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            String[] header = {"Course Code", "Course Title", "Credits", "Semester", "Year", "Final Grade"};
            writer.writeNext(header);
            
            for (Enrollment enrollment : enrollments) {
           
            	Double finalGrade = grades.stream()
                    .filter(g -> g.getEnrollmentId() == enrollment.getEnrollmentId())
                    .filter(g -> g.getFinalGrade() != null)
                    .map(Grade::getFinalGrade)
                    .findFirst()
                    .orElse(null);
                
                String[] data = {
                    "COURSE_CODE", 
                    "Course Title", 
                    "3", 
                    "FALL", 
                    "2024", 
                    finalGrade != null ? String.format("%.2f", finalGrade) : "N/A"
                };
                writer.writeNext(data);
            }
            
            logger.info("Transcript exported successfully to: " + filePath);
            return true;
            
        } catch (IOException e) {
            logger.severe("Failed to export transcript: " + e.getMessage());
            return false;
        }
    }
    
    public static boolean exportCourseList(List<Object> courses, String filePath) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(filePath))) {
            String[] header = {"Course Code", "Course Title", "Credits", "Instructor", "Capacity"};
            writer.writeNext(header);
            
            logger.info("Course list exported successfully to: " + filePath);
            return true;
            
        } catch (IOException e) {
            logger.severe("Failed to export course list: " + e.getMessage());
            return false;
        }
    }

	public static boolean exportGradeSheet(List<Enrollment> enrollments, String filePath) {
		return false;
	}

	
}