package edu.univ.erp.ui.student;
import edu.univ.erp.ui.common.MainFrame;
import edu.univ.erp.domain.Student;

import javax.swing.*;
import java.awt.*;

public class StudentDashboard extends MainFrame {
    private JTabbedPane tabbedPane;
    private Student currentStudent;
    public StudentDashboard() {
        super("Student Dashboard");
        this.currentStudent = sessionManager.getCurrentStudent();
        System.out.println(this.currentStudent);
        
        if (currentStudent == null) {
            JOptionPane.showMessageDialog(this, 
                "Error: Could not load student profile. Please login again.", 
                "Error", JOptionPane.ERROR_MESSAGE);
            logout();
            return;
        }
     initializeUI();  
    }
    
        
    
    @Override
    protected JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        
        tabbedPane = new JTabbedPane();
        
        tabbedPane.addTab("📚 Course Catalog", new CourseCatalogPanel(currentStudent));
        tabbedPane.addTab("📝 My Registrations", new RegistrationPanel(currentStudent));
        tabbedPane.addTab("🕒 Timetable", new TimetablePanel(currentStudent));
        tabbedPane.addTab("📊 Grades", new GradesPanel(currentStudent));
        tabbedPane.addTab("📄 Transcript", new TranscriptPanel(currentStudent));
        
        contentPanel.add(tabbedPane, BorderLayout.CENTER);
        
        return contentPanel;
    }
    
    
    @Override
    protected void updateUserInfo() {
        if (sessionManager.isLoggedIn() && currentStudent != null) {
            String userInfo = String.format("Welcome, %s (%s) | Roll No: %s | Program: %s", 
                sessionManager.getCurrentUsername(),
                sessionManager.getCurrentUserRole().getDisplayName(),
                currentStudent.getRollNo(),
                currentStudent.getProgram());
            userInfoLabel.setText(userInfo);
        }
    }
    
}



