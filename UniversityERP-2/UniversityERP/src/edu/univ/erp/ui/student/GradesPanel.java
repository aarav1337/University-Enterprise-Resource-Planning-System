package edu.univ.erp.ui.student;

import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.GradeComponent;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.api.StudentAPI;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.data.CourseDAO;
import edu.univ.erp.data.SectionDAO;
import edu.univ.erp.util.MessageHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.*;
import java.util.stream.Collectors;

public class GradesPanel extends JPanel {
    private Student student;
    private StudentAPI studentAPI;
    private CourseDAO courseDAO;
    private SectionDAO sectionDAO;
    private JTable gradesTable;
    private DefaultTableModel tableModel;
    private JLabel summaryLabel;
    
    public GradesPanel(Student currentStudent) {
        this.student = SessionManager.getInstance().getCurrentStudent();
        this.studentAPI = new StudentAPI();
        this.courseDAO = new CourseDAO();
        this.sectionDAO = new SectionDAO();
        initializeUI();
        loadGrades();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        JLabel titleLabel = new JLabel("My Grades", SwingConstants.LEFT);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        headerPanel.add(titleLabel, BorderLayout.WEST);
        
        summaryLabel = new JLabel("", SwingConstants.RIGHT);
        summaryLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        headerPanel.add(summaryLabel, BorderLayout.EAST);
        
        add(headerPanel, BorderLayout.NORTH);
        
        String[] columns = {"Course Code", "Course Title", "Credits", "Quiz", "Midterm", "End Semester", "Final Grade", "Letter Grade"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                return column >= 3 ? Double.class : String.class;
            }
        };
        
        gradesTable = new JTable(tableModel);
        gradesTable.setRowHeight(25);
        gradesTable.setAutoCreateRowSorter(true);
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 3; i < gradesTable.getColumnCount(); i++) {
            gradesTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        JScrollPane scrollPane = new JScrollPane(gradesTable);
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton refreshBtn = new JButton("🔄 Refresh Grades");
        refreshBtn.addActionListener(e -> loadGrades());
        
        buttonPanel.add(refreshBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadGrades() {
        try {
            var gradesResult = studentAPI.getMyGrades(student.getUserId());
            var enrollmentsResult = studentAPI.getMyEnrollments(student.getUserId());
            
            if (!gradesResult.isSuccess() || !enrollmentsResult.isSuccess()) {
                MessageHelper.showError(this, "Failed to load grades data");
                return;
            }
            
            List<Grade> grades = gradesResult.getData();
            List<Enrollment> enrollments = enrollmentsResult.getData();
            
            tableModel.setRowCount(0);
            
            Map<Integer, Map<GradeComponent, Double>> gradeMap = new HashMap<>();
            Map<Integer, Double> finalGrades = new HashMap<>();
            
            for (Grade grade : grades) {
                gradeMap.putIfAbsent(grade.getEnrollmentId(), new HashMap<>());
                gradeMap.get(grade.getEnrollmentId()).put(grade.getComponent(), grade.getScore());
                
                if (grade.getFinalGrade() != null) {
                    finalGrades.put(grade.getEnrollmentId(), grade.getFinalGrade());
                }
            }
            
            double totalCredits = 0;
            double totalGradePoints = 0;
            int gradedCourses = 0;
            
            for (Enrollment enrollment : enrollments) {
                Section section = sectionDAO.getSectionById(enrollment.getSectionId());
                if (section == null) continue;
                
                String courseCode = section.getCourseCode();
                var course = courseDAO.getCourseByCode(courseCode);
                if (course == null) continue;
                
                Map<GradeComponent, Double> courseGrades = gradeMap.get(enrollment.getEnrollmentId());
                Double finalGrade = finalGrades.get(enrollment.getEnrollmentId());
                
                Double quizScore = courseGrades != null ? courseGrades.get(GradeComponent.QUIZ) : null;
                Double midtermScore = courseGrades != null ? courseGrades.get(GradeComponent.MIDTERM) : null;
                Double endSemScore = courseGrades != null ? courseGrades.get(GradeComponent.END_SEM) : null;
                
                String letterGrade = calculateLetterGrade(finalGrade);
                
                Object[] row = {
                    courseCode,
                    course.getTitle(),
                    course.getCredits(),
                    formatScore(quizScore),
                    formatScore(midtermScore),
                    formatScore(endSemScore),
                    formatScore(finalGrade),
                    letterGrade
                };
                tableModel.addRow(row);
                
                if (finalGrade != null) {
                    totalCredits += course.getCredits();
                    totalGradePoints += (finalGrade / 20.0) * course.getCredits(); 
                    gradedCourses++;
                }
            }
            
            updateSummary(totalCredits, totalGradePoints, gradedCourses);
            
        } catch (Exception e) {
            MessageHelper.showError(this, "Error loading grades: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private String formatScore(Double score) {
        return score != null ? String.format("%.2f", score) : "N/A";
    }
    
    private String calculateLetterGrade(Double finalGrade) {
        if (finalGrade == null) return "N/A";
        if (finalGrade >= 90) return "A";
        if (finalGrade >= 80) return "B";
        if (finalGrade >= 70) return "C";
        if (finalGrade >= 60) return "D";
        return "F";
    }
    
    private void updateSummary(double totalCredits, double totalGradePoints, int gradedCourses) {
        double gpa = totalCredits > 0 ? totalGradePoints / totalCredits : 0.0;
        
        String summary = String.format("GPA: %.2f | Completed Courses: %d | Total Credits: %.0f", 
                                     gpa, gradedCourses, totalCredits);
        summaryLabel.setText(summary);
    }
}