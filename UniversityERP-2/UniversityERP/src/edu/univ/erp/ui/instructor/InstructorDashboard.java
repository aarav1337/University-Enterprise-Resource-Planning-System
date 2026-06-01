package edu.univ.erp.ui.instructor;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import edu.univ.erp.api.InstructorAPI;
import edu.univ.erp.api.InstructorAPI.ApiResult;
import edu.univ.erp.api.StudentAPI;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.Enrollment;
import edu.univ.erp.domain.Grade;
import edu.univ.erp.domain.GradeComponent;
import edu.univ.erp.domain.Section;
import edu.univ.erp.service.InstructorService.ClassStatistics;
import edu.univ.erp.ui.common.MainFrame;
import edu.univ.erp.ui.common.TableHelper;

public class InstructorDashboard extends MainFrame {
    private JTabbedPane tabbedPane;
    private InstructorAPI instructorAPI;
    private SessionManager sessionManager;
    private StudentAPI studentAPI;
    
    private JTable sectionsTable;
    private JTable enrollmentsTable;
    private JTextArea statisticsArea;
    
    private List<Section> currentSections;
    private List<Enrollment> currentEnrollments;
    private Section selectedSection;
    
    public InstructorDashboard() {
        super("Instructor Dashboard");
        this.instructorAPI = new InstructorAPI();
        this.sessionManager = SessionManager.getInstance();
        this.studentAPI = new StudentAPI();
        initializeDashboard();
    }
    
    private void initializeDashboard() {
        setSize(1000, 700);
        setLocationRelativeTo(null);
        loadInstructorData();
    }
    
    @Override
    protected JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        
        tabbedPane = new JTabbedPane();
        
        tabbedPane.addTab("My Sections", createSectionsPanel());
        tabbedPane.addTab("Grade Management", createGradeManagementPanel());
        tabbedPane.addTab("Class Statistics", createStatisticsPanel());
        
        contentPanel.add(tabbedPane, BorderLayout.CENTER);
        
        return contentPanel;
    }
    
    private void loadInstructorData() {
        if (sessionManager.getCurrentInstructor() != null) {
            ApiResult<List<Section>> result = 
                instructorAPI.getMySections(sessionManager.getCurrentInstructor().getUserId());
            
            if (result.isSuccess()) {
                currentSections = result.getData();
                System.out.println("DEBUG: Loaded " + currentSections.size() + " sections for instructor");
                refreshSectionsTable();
                showMessage(result.getMessage());
            } else {
                showError(result.getMessage());
            }
        } else {
            System.out.println("DEBUG: No instructor found in session");
        }
    }
    
    // My Sections Panel
    private JPanel createSectionsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("My Teaching Sections", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        String[] columnNames = {"Section ID", "Course Code", "Day", "Time", "Room", "Enrolled", "Capacity", "Status"};
        DefaultTableModel model = TableHelper.createTableModel(columnNames);
        sectionsTable = TableHelper.createSortableTable(model);
        
        sectionsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                onSectionSelected();
            }
        });
        
        JScrollPane scrollPane = TableHelper.createScrollPaneWithTable(sectionsTable);
        panel.add(scrollPane, BorderLayout.CENTER);
        
        JPanel buttonPanel = new JPanel(new FlowLayout());
        JButton viewGradesBtn = new JButton("View Grades for Selected Section");
        JButton refreshBtn = new JButton("Refresh");
        
        viewGradesBtn.addActionListener(e -> viewGradesForSelectedSection());
        refreshBtn.addActionListener(e -> loadInstructorData());
        
        buttonPanel.add(viewGradesBtn);
        buttonPanel.add(refreshBtn);
        panel.add(buttonPanel, BorderLayout.SOUTH);
        
        return panel;
    }
    
   //Grade Management Panel
 
    private JPanel createGradeManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectionPanel.add(new JLabel("Select Section:"));
        
        JComboBox<String> sectionCombo = new JComboBox<>();
        selectionPanel.add(sectionCombo);
        
        JButton loadEnrollmentsBtn = new JButton("Load Students");
        selectionPanel.add(loadEnrollmentsBtn);
        
        JButton refreshSectionsBtn = new JButton("Refresh Sections");
        selectionPanel.add(refreshSectionsBtn);
        
        panel.add(selectionPanel, BorderLayout.NORTH);
        
        String[] gradeColumns = {
        	    "Enrollment ID", "Student ID", "Roll No", "Student Name", "Status",
        	    "Quiz (20%)", "Midterm (30%)", "End Sem (50%)", "Final Grade", "Actions"
        	};
        
        DefaultTableModel gradeModel = new DefaultTableModel(gradeColumns, 0) {
        	@Override
        	public boolean isCellEditable(int row, int column) {
        		boolean hasFinalGrade = checkIfRowHasFinalGrade(row);
        		 if (hasFinalGrade) {
        	            return false; 
        	        }
        	    return column >= 5 && column <= 7 || column ==9;
        	}
            
        	@Override
            public Class<?> getColumnClass(int column) {
                switch (column) {
                    case 0: 
                        return Integer.class;
                    case 1: 
                        return Integer.class;
                    case 5: 
                    case 6: 
                    case 7: 
                    case 8: 
                        return Object.class;
                    case 9: 
                        return JButton.class;
                    default:
                        return String.class;
                }
            }
        };
        
        
        
        
        enrollmentsTable = TableHelper.createSortableTable(gradeModel);
        
        setupGradeTableRenderer();
        
        setupActionColumn();
        enrollmentsTable.setDefaultEditor(Object.class, new GradeCellEditor());
        
        JScrollPane enrollmentsScroll = TableHelper.createScrollPaneWithTable(enrollmentsTable);
        panel.add(enrollmentsScroll, BorderLayout.CENTER);
        
        JPanel actionPanel = createGradeActionPanel();
        panel.add(actionPanel, BorderLayout.SOUTH);
        
        JPanel infoPanel = new JPanel(new BorderLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Section Information"));
        JLabel sectionInfoLabel = new JLabel("Please select a section to view enrollments");
        infoPanel.add(sectionInfoLabel, BorderLayout.CENTER);
        panel.add(infoPanel, BorderLayout.SOUTH);
        
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1) { 
                refreshSectionComboBox(sectionCombo);
            }
        });
        
        // Load sections into combo box
        
        loadEnrollmentsBtn.addActionListener(e -> {
            int selectedIndex = sectionCombo.getSelectedIndex();
            if (selectedIndex >= 0 && currentSections != null && selectedIndex < currentSections.size()) {
                selectedSection = currentSections.get(selectedIndex);
                loadEnrollmentsAndGradesForSection(selectedSection.getSectionId()); 
                sectionInfoLabel.setText("Section: " + selectedSection.getCourseCode() + 
                                       " | Room: " + selectedSection.getRoom() + 
                                       " | Time: " + selectedSection.getDay() + " " + selectedSection.getTime());
            } else {
                showError("Please select a valid section first.");
            }
        });
        
        refreshSectionsBtn.addActionListener(e -> {
            loadInstructorData(); // Reload all data
            refreshSectionComboBox(sectionCombo);
            showMessage("Sections refreshed");
        });
        
        refreshSectionComboBox(sectionCombo);
        
        return panel;
    }    
    
    
    private boolean checkIfRowHasFinalGrade(int row) {
        if (enrollmentsTable == null) return false;
        
        Object finalGradeValue = enrollmentsTable.getValueAt(row, 8);
        if (finalGradeValue instanceof String) {
            String finalGradeStr = (String) finalGradeValue;
            return !finalGradeStr.isEmpty() && !finalGradeStr.trim().isEmpty();
        }
        return false;
    }
    
    private void setupActionColumn() {
       enrollmentsTable.getColumnModel().getColumn(9).setCellRenderer(new ButtonRenderer());
        enrollmentsTable.getColumnModel().getColumn(9).setCellEditor(new ButtonEditor(new JCheckBox()));
        
        enrollmentsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                int column = enrollmentsTable.columnAtPoint(e.getPoint());
                int row = enrollmentsTable.rowAtPoint(e.getPoint());
                
                if (column == 9 && row >= 0) { 
                    String buttonText = (String) enrollmentsTable.getValueAt(row, 9);
                    if ("Compute Final".equals(buttonText)) {
                        handleComputeButtonClick(row);
                    } else if ("✅ Computed".equals(buttonText)) {
                        handleViewDetailsClick(row);
                    }
                }
            }
        });
    }
    
    private void handleComputeButtonClick(int row) {
        computeFinalGradeForRow(row);
    }
    private void handleViewDetailsClick(int row) {
        DefaultTableModel model = (DefaultTableModel) enrollmentsTable.getModel();
        int enrollmentId = (Integer) model.getValueAt(row, 0);
        int studentId = (Integer) model.getValueAt(row, 1);
        String finalGrade = (String) model.getValueAt(row, 8);
        
        showStudentGradeDetails(enrollmentId, studentId, finalGrade);
    }
    private void showStudentGradeDetails(int enrollmentId, int studentId, String finalGrade) {
        JPanel detailsPanel = new JPanel(new GridLayout(0, 2, 5, 5));
        
        detailsPanel.add(new JLabel("Enrollment ID:"));
        detailsPanel.add(new JLabel(String.valueOf(enrollmentId)));
        
        detailsPanel.add(new JLabel("Student ID:"));
        detailsPanel.add(new JLabel(String.valueOf(studentId)));
        
        detailsPanel.add(new JLabel("Final Grade:"));
        detailsPanel.add(new JLabel(finalGrade));
        
        detailsPanel.add(new JLabel("Status:"));
        detailsPanel.add(new JLabel("Grade Computed ✅"));
        
        JOptionPane.showMessageDialog(this, detailsPanel, 
            "Student Grade Details", JOptionPane.INFORMATION_MESSAGE);
    }
    
    
    private void setupGradeTableRenderer() {
        enrollmentsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (column >= 5 && column <= 8) {
                    setHorizontalAlignment(JLabel.RIGHT);
                    
                   if (value instanceof Double) {
                        setText(String.format("%.2f", (Double) value));
                    } else if (value instanceof String) {
                        setText((String) value);
                    } else if (value == null) {
                        setText("");
                    } else {
                        setText(value.toString());
                    }
                } else {
                    setHorizontalAlignment(JLabel.LEFT);
                }
                
                return c;
            }
        });
    }
    
    
    private JPanel createGradeActionPanel() {
        JPanel actionPanel = new JPanel(new FlowLayout());
        actionPanel.setBorder(BorderFactory.createTitledBorder("Grade Actions"));
        
        JButton saveAllGradesBtn = new JButton("Save All Grades");
        JButton computeAllFinalBtn = new JButton("Compute All Final Grades");
        JButton exportGradesBtn = new JButton("Export Grades to CSV");
        JButton importGradesBtn = new JButton("Import Grades from CSV");
        JButton clearGradesBtn = new JButton("Clear All Grades");
        
        saveAllGradesBtn.addActionListener(e -> saveAllGrades());
        computeAllFinalBtn.addActionListener(e -> computeAllFinalGrades());
        exportGradesBtn.addActionListener(e -> exportGradesToCSV());
        importGradesBtn.addActionListener(e -> importGradesFromCSV());
        clearGradesBtn.addActionListener(e -> clearAllGrades());
        
        actionPanel.add(saveAllGradesBtn);
        actionPanel.add(computeAllFinalBtn);
        actionPanel.add(exportGradesBtn);
        actionPanel.add(importGradesBtn);
        actionPanel.add(clearGradesBtn);
        
        return actionPanel;
    }
    
 // Statistics Panel
    private JPanel createStatisticsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel selectionPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        selectionPanel.add(new JLabel("Select Section:"));
        
        JComboBox<String> statsSectionCombo = new JComboBox<>();
        selectionPanel.add(statsSectionCombo);
        
        JButton generateStatsBtn = new JButton("Generate Statistics");
        JButton refreshBtn = new JButton("Refresh");
        selectionPanel.add(generateStatsBtn);
        selectionPanel.add(refreshBtn);
        
        panel.add(selectionPanel, BorderLayout.NORTH);
        
        statisticsArea = new JTextArea(20, 60);
        statisticsArea.setEditable(false);
        statisticsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        statisticsArea.setText("Please select a section and click 'Generate Statistics'");
        
        JScrollPane statsScroll = new JScrollPane(statisticsArea);
        panel.add(statsScroll, BorderLayout.CENTER);
        
        JPanel statsButtonPanel = new JPanel(new FlowLayout());
        JButton exportStatsBtn = new JButton("Export Report");
        statsButtonPanel.add(exportStatsBtn);
        panel.add(statsButtonPanel, BorderLayout.SOUTH);
        
        generateStatsBtn.addActionListener(e -> {
            int selectedIndex = statsSectionCombo.getSelectedIndex();
            if (selectedIndex >= 0 && currentSections != null && selectedIndex < currentSections.size()) {
                Section selected = currentSections.get(selectedIndex);
                selectedSection = selected;
                generateSimpleStatistics(selected.getSectionId());
            } else {
                showError("Please select a valid section first.");
            }
        });
        
        refreshBtn.addActionListener(e -> {
            refreshSectionComboBox(statsSectionCombo);
            showMessage("Sections list refreshed");
        });
        
        exportStatsBtn.addActionListener(e -> exportStatistics());
        
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 2) { 
                refreshSectionComboBox(statsSectionCombo);
            }
        });
        
        refreshSectionComboBox(statsSectionCombo);
        
        return panel;
    }
    
    
    
    private void generateSimpleStatistics(int sectionId) {
        System.out.println("Generating statistics for section: " + sectionId);
        
        ApiResult<ClassStatistics> statsResult = instructorAPI.getClassStatistics(sectionId);
        
        if (statsResult.isSuccess() && statsResult.getData() != null) {
            ClassStatistics stats = statsResult.getData();
            displaySimpleStatistics(stats, sectionId);
            showMessage("Statistics generated successfully");
        } else {
            statisticsArea.setText("Error: Unable to generate statistics\n" + 
                                  (statsResult != null ? statsResult.getMessage() : "No data available"));
            showError("Failed to generate statistics");
        }
    }
    
    
    private void displaySimpleStatistics(ClassStatistics stats, int sectionId) {
        StringBuilder sb = new StringBuilder();
        
        sb.append("CLASS STATISTICS REPORT\n");
        sb.append("=======================\n\n");
        
        sb.append("Course: ").append(stats.getSection().getCourseCode()).append("\n");
        sb.append("Section: ").append(stats.getSection().getSectionId()).append("\n");
        sb.append("Room: ").append(stats.getSection().getRoom()).append("\n");
        sb.append("Time: ").append(stats.getSection().getDay()).append(" ").append(stats.getSection().getTime()).append("\n\n");
        
        sb.append("ENROLLMENT\n");
        sb.append("----------\n");
        sb.append("Students: ").append(stats.getTotalStudents()).append("/").append(stats.getSection().getCapacity());
        sb.append(" (").append(String.format("%.1f%%", stats.getEnrollmentRate())).append(")\n");
        sb.append("Available: ").append(stats.getSection().getAvailableSeats()).append(" seats\n\n");
        
        sb.append("PERFORMANCE\n");
        sb.append("-----------\n");
        sb.append("Average Grade: ").append(String.format("%.2f/100", stats.getAverageGrade())).append("\n");
        
        Map<String, Integer> distribution = calculateSimpleGradeDistribution(sectionId);
        sb.append("\nGRADE DISTRIBUTION\n");
        sb.append("-----------------\n");
        
        int totalWithGrades = distribution.values().stream().mapToInt(Integer::intValue).sum();
        
        for (String grade : new String[]{"A", "B", "C", "D", "F"}) {
            int count = distribution.getOrDefault(grade, 0);
            double percentage = totalWithGrades > 0 ? (count * 100.0) / totalWithGrades : 0;
            sb.append(String.format("%s: %d students (%.1f%%)\n", grade, count, percentage));
        }
        
        sb.append("\nSUMMARY\n");
        sb.append("-------\n");
        sb.append("Total students with grades: ").append(totalWithGrades).append("\n");
        
        if (totalWithGrades > 0) {
            int passing = distribution.getOrDefault("A", 0) + distribution.getOrDefault("B", 0) + 
                         distribution.getOrDefault("C", 0) + distribution.getOrDefault("D", 0);
            double passRate = (passing * 100.0) / totalWithGrades;
            sb.append("Passing rate (A-D): ").append(String.format("%.1f%%", passRate)).append("\n");
        }
        
        statisticsArea.setText(sb.toString());
    }
    
    
    private Map<String, Integer> calculateSimpleGradeDistribution(int sectionId) {
        Map<String, Integer> distribution = new HashMap<>();
        distribution.put("A", 0);
        distribution.put("B", 0);
        distribution.put("C", 0);
        distribution.put("D", 0);
        distribution.put("F", 0);
        
        InstructorAPI.ApiResult<List<Enrollment>> enrollmentsResult = 
            instructorAPI.getSectionEnrollments(sectionId, sessionManager.getCurrentInstructor().getUserId());
        
        if (enrollmentsResult.isSuccess() && enrollmentsResult.getData() != null) {
            for (Enrollment enrollment : enrollmentsResult.getData()) {
                Double finalGrade = getExistingFinalGrade(enrollment.getEnrollmentId());
                if (finalGrade != null) {
                    String gradeLetter = convertToGradeLetter(finalGrade);
                    distribution.put(gradeLetter, distribution.get(gradeLetter) + 1);
                }
            }
        }
        
        return distribution;
    }

    private String convertToGradeLetter(double grade) {
        if (grade >= 90) return "A";
        if (grade >= 80) return "B";
        if (grade >= 70) return "C";
        if (grade >= 60) return "D";
        return "F";
    }
    
    
    private void exportStatistics() {
        if (selectedSection == null) {
            showError("Please select a section first and generate statistics.");
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Statistics Report");
        fileChooser.setSelectedFile(new File("statistics_section_" + selectedSection.getSectionId() + ".pdf"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            InstructorAPI.ApiResult<Boolean> result = 
                instructorAPI.exportClassReport(selectedSection.getSectionId(), 
                    fileChooser.getSelectedFile().getAbsolutePath());
            
            if (result.isSuccess()) {
                showMessage("Statistics report exported successfully!");
            } else {
                showError("Export failed: " + result.getMessage());
            }
        }
    }
    
    private JTable createPerformanceTable() {
        String[] columns = {"Student ID", "Roll No", "Student Name", "Quiz", "Midterm", "End Sem", "Final Grade", "Grade Letter"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make table read-only
            }
        };
        
        JTable table = TableHelper.createSortableTable(model);
        table.setRowHeight(25);
        return table;
    }
    
    
    private void debugGrades(int enrollmentId) {
        System.out.println("=== DEBUG Grades for Enrollment " + enrollmentId + " ===");
        
        for (GradeComponent component : GradeComponent.values()) {
            InstructorAPI.ApiResult<Double> result = instructorAPI.getExistingGrade(enrollmentId, component);
            System.out.println("Component " + component + ": " + 
                (result.isSuccess() ? result.getData() : "Error - " + result.getMessage()));
        }
        
        InstructorAPI.ApiResult<Double> finalResult = instructorAPI.getExistingFinalGrade(enrollmentId);
        System.out.println("Final Grade: " + 
            (finalResult.isSuccess() ? finalResult.getData() : "Error - " + finalResult.getMessage()));
    }
    
    private void onSectionSelected() {
        int selectedRow = sectionsTable.getSelectedRow();
        if (selectedRow >= 0) {
        }
    }
    
    private void viewGradesForSelectedSection() {
        int selectedRow = sectionsTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = sectionsTable.convertRowIndexToModel(selectedRow);
            int sectionId = (Integer) sectionsTable.getModel().getValueAt(modelRow, 0);
            
            selectedSection = currentSections.stream()
                .filter(s -> s.getSectionId() == sectionId)
                .findFirst()
                .orElse(null);
                
            if (selectedSection != null) {
                loadEnrollmentsForSection(sectionId);
                tabbedPane.setSelectedIndex(1); // Switch to grade management tab
                showMessage("Loaded enrollments for section: " + selectedSection.getCourseCode());
            }
        } else {
            showError("Please select a section first.");
        }
    }
    
    private void loadEnrollmentsForSection(int sectionId) {
        if (sessionManager.getCurrentInstructor() != null) {
            InstructorAPI.ApiResult<List<Enrollment>> result = 
                instructorAPI.getSectionEnrollments(sectionId, sessionManager.getCurrentInstructor().getUserId());
            
            if (result.isSuccess()) {
                currentEnrollments = result.getData();
                refreshEnrollmentsTable();
                showMessage(result.getMessage());
            } else {
                showError(result.getMessage());
            }
        }
    }
    
    private void generateStatistics(int sectionId) {
        ApiResult<ClassStatistics> result = 
            instructorAPI.getClassStatistics(sectionId);
        
        if (result.isSuccess()) {
            ClassStatistics stats = result.getData();
            displayStatistics(stats);
            showMessage(result.getMessage());
        } else {
            statisticsArea.setText("Error: " + result.getMessage());
            showError(result.getMessage());
        }
    }
    
    
    private void refreshSectionsTable() {
        if (sectionsTable == null) return;
        
        DefaultTableModel model = (DefaultTableModel) sectionsTable.getModel();
        model.setRowCount(0);
        
        if (currentSections != null) {
            for (Section section : currentSections) {
                String status = section.hasAvailableSeats() ? "Open" : "Full";
                String statusColor = section.hasAvailableSeats() ? "🟢" : "🔴";
                
                model.addRow(new Object[]{
                    section.getSectionId(),
                    section.getCourseCode(),
                    section.getDay().toString(),
                    section.getTime(),
                    section.getRoom(),
                    section.getEnrolledCount(),
                    section.getCapacity(),
                    statusColor + " " + status
                });
            }
        }
    }
    
   
    
    
    private void refreshEnrollmentsTable() {
        if (enrollmentsTable == null) return;
        
        DefaultTableModel model = (DefaultTableModel) enrollmentsTable.getModel();
        model.setRowCount(0);
        
        if (currentEnrollments != null) {
            for (Enrollment enrollment : currentEnrollments) {
                debugGrades(enrollment.getEnrollmentId());
                
                String studentName = getStudentName(enrollment.getStudentId());
                String rollNo = getStudentRollNo(enrollment.getStudentId());
                
                Double quizGrade = getExistingGrade(enrollment.getEnrollmentId(), GradeComponent.QUIZ);
                Double midtermGrade = getExistingGrade(enrollment.getEnrollmentId(), GradeComponent.MIDTERM);
                Double endSemGrade = getExistingGrade(enrollment.getEnrollmentId(), GradeComponent.END_SEM);
                Double finalGrade = getExistingFinalGrade(enrollment.getEnrollmentId());
                boolean hasFinalGrade = checkIfFinalGradeExists(enrollment.getEnrollmentId());
                
                System.out.println("End Sem Grade retrieved: " + endSemGrade);
                
                model.addRow(new Object[]{
                    enrollment.getEnrollmentId(),
                    enrollment.getStudentId(),
                    rollNo,
                    studentName,
                    enrollment.getStatus().toString(),
                    quizGrade != null ? quizGrade : "", 
                    midtermGrade != null ? midtermGrade : "", 
                    endSemGrade != null ? endSemGrade : "", 
                    finalGrade != null ? String.format("%.2f", finalGrade) : "", 
                    hasFinalGrade ? "✅ Computed" : "Compute Final" 
                });
            }
        }
    }
    
    private String getStudentName(int studentId) {
        try {
        	StudentAPI.ApiResult<String> infoResult = studentAPI.getStudentName(studentId);
            return infoResult.getData(); 
        } catch (Exception e) {
            return "Unknown Student";
        }
    }

    private String getStudentRollNo(int studentId) {
        try {
        	StudentAPI.ApiResult<String> infoResult = studentAPI.getStudentRollNo(studentId);
            
            return "ROLL_" + infoResult.getData(); 
        } catch (Exception e) {
            return "N/A";
        }
    }
    
    
    
    private void refreshSectionComboBox(JComboBox<String> comboBox) {
        System.out.println("DEBUG: Refreshing combo box. Current sections: " + 
                          (currentSections != null ? currentSections.size() : 0));
        
        comboBox.removeAllItems();
        if (currentSections != null && !currentSections.isEmpty()) {
            for (Section section : currentSections) {
                String item = String.format("%s - %s %s (%d/%d)", 
                    section.getCourseCode(),
                    section.getDay().toString(),
                    section.getTime(),
                    section.getEnrolledCount(),
                    section.getCapacity());
                comboBox.addItem(item);
                System.out.println("DEBUG: Added section to combo: " + item);
            }
            comboBox.setSelectedIndex(0);
            System.out.println("DEBUG: Combo box populated with " + currentSections.size() + " sections");
        } else {
            System.out.println("DEBUG: No sections available to populate combo box");
            comboBox.addItem("No sections available");
        }
    }
    
    
    private void displayStatistics(ClassStatistics stats) {
        if (stats != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("=== CLASS STATISTICS ===\n\n");
            sb.append("Section: ").append(stats.getSection().getCourseCode()).append("\n");
            sb.append("Enrollment: ").append(stats.getTotalStudents()).append("/")
              .append(stats.getSection().getCapacity()).append(" (")
              .append(String.format("%.1f", stats.getEnrollmentRate())).append("%)\n");
            sb.append("Available Seats: ").append(stats.getSection().getAvailableSeats()).append("\n");
            sb.append("Average Grade: ").append(String.format("%.2f", stats.getAverageGrade())).append("/100\n\n");
            sb.append("More detailed statistics coming soon...");
            
            statisticsArea.setText(sb.toString());
        } else {
            statisticsArea.setText("No statistics available for this section.");
        }
    }
    
    public static class InstructorService {
        public static class ClassStatistics {
            private Section section;
            private int totalStudents;
            private double averageGrade;
            private double enrollmentRate;
            
            public ClassStatistics(Section section, int totalStudents, double averageGrade, double enrollmentRate) {
                this.section = section;
                this.totalStudents = totalStudents;
                this.averageGrade = averageGrade;
                this.enrollmentRate = enrollmentRate;
            }
            
            public Section getSection() { return section; }
            public int getTotalStudents() { return totalStudents; }
            public double getAverageGrade() { return averageGrade; }
            public double getEnrollmentRate() { return enrollmentRate; }
            
            @Override
            public String toString() {
                return String.format(
                    "Section: %s\nEnrolled: %d/%d (%.1f%%)\nAvailable: %d\nAverage Grade: %.2f",
                    section.getCourseCode(),
                    totalStudents,
                    section.getCapacity(),
                    enrollmentRate,
                    section.getAvailableSeats(),
                    averageGrade
                );
            }
        }
    }
    
    
    
    private void loadEnrollmentsAndGradesForSection(int sectionId) {
        if (sessionManager.getCurrentInstructor() != null) {
            InstructorAPI.ApiResult<List<Enrollment>> result = 
                instructorAPI.getSectionEnrollments(sectionId, sessionManager.getCurrentInstructor().getUserId());
            
            if (result.isSuccess()) {
                currentEnrollments = result.getData();
                loadGradesForEnrollments(); 
             
                refreshEnrollmentsTable();
                showMessage(result.getMessage());
            } else {
                showError(result.getMessage());
            }
        }
    }

    private void loadGradesForEnrollments() {
     
    	if (currentEnrollments == null) return;
        
        for (Enrollment enrollment : currentEnrollments) {
            InstructorAPI.ApiResult<List<Grade>> gradesResult = 
                instructorAPI.getGradesByEnrollment(enrollment.getEnrollmentId());
            
            if (gradesResult.isSuccess() && gradesResult.getData() != null) {
            }
        }
    }

   
    
    private void refreshGradesTable() {
        if (enrollmentsTable == null) return;
        
        DefaultTableModel model = (DefaultTableModel) enrollmentsTable.getModel();
        model.setRowCount(0);
        
        if (currentEnrollments != null) {
            for (Enrollment enrollment : currentEnrollments) {
                // Check if final grade exists in database
                boolean hasFinalGrade = checkIfFinalGradeExists(enrollment.getEnrollmentId());
                
                // Get existing component grades
                Double quizGrade = getExistingGrade(enrollment.getEnrollmentId(), GradeComponent.QUIZ);
                Double midtermGrade = getExistingGrade(enrollment.getEnrollmentId(), GradeComponent.MIDTERM);
                Double endSemGrade = getExistingGrade(enrollment.getEnrollmentId(), GradeComponent.END_SEM);
                Double finalGrade = getExistingFinalGrade(enrollment.getEnrollmentId());
                
                model.addRow(new Object[]{
                    enrollment.getEnrollmentId(),
                    enrollment.getStudentId(),
                    "ROLL_" + enrollment.getStudentId(),
                    "Student " + enrollment.getStudentId(),
                    enrollment.getStatus().toString(),
                    quizGrade != null ? String.format("%.2f", quizGrade) : "", // Quiz
                    midtermGrade != null ? String.format("%.2f", midtermGrade) : "", // Midterm  
                    endSemGrade != null ? String.format("%.2f", endSemGrade) : "", // End sem
                    finalGrade != null ? String.format("%.2f", finalGrade) : "", // Final grade
                    hasFinalGrade ? "✅ Computed" : "Compute Final" // Action button state
                });
            }
        }
    }

    private Double getExistingGrade(int enrollmentId, GradeComponent component) {
        InstructorAPI.ApiResult<Double> result = instructorAPI.getExistingGrade(enrollmentId, component);
        return result.isSuccess() ? result.getData() : null;
    }

    private Double getExistingFinalGrade(int enrollmentId) {
        InstructorAPI.ApiResult<Double> result = instructorAPI.getExistingFinalGrade(enrollmentId);
        return result.isSuccess() ? result.getData() : null;
    }

    private boolean checkIfFinalGradeExists(int enrollmentId) {
        InstructorAPI.ApiResult<Boolean> result = instructorAPI.checkIfFinalGradeExists(enrollmentId);
        return result.isSuccess() && Boolean.TRUE.equals(result.getData());
    }    
        
    
    
    private void saveAllGrades() {
        if (selectedSection == null) {
            showError("Please select a section first.");
            return;
        }
        
        DefaultTableModel model = (DefaultTableModel) enrollmentsTable.getModel();
        int savedCount = 0;
        int errorCount = 0;
        
        for (int row = 0; row < model.getRowCount(); row++) {
            int enrollmentId = (Integer) model.getValueAt(row, 0);
            
            try {
                Object quizObj = model.getValueAt(row, 5);    
                Object midtermObj = model.getValueAt(row, 6);  
                Object endSemObj = model.getValueAt(row, 7);  
                
                Double quizGrade = convertToDouble(quizObj);
                Double midtermGrade = convertToDouble(midtermObj);
                Double endSemGrade = convertToDouble(endSemObj);
                
                if (quizGrade != null) {
                    boolean success = instructorAPI.enterGrade(
                        enrollmentId, GradeComponent.QUIZ, quizGrade, 
                        sessionManager.getCurrentInstructor().getUserId()).isSuccess();
                    if (success) savedCount++;
                    else errorCount++;
                }
                
                if (midtermGrade != null) {
                    boolean success = instructorAPI.enterGrade(
                        enrollmentId, GradeComponent.MIDTERM, midtermGrade, 
                        sessionManager.getCurrentInstructor().getUserId()).isSuccess();
                    if (success) savedCount++;
                    else errorCount++;
                }
                
                if (endSemGrade != null) {
                    boolean success = instructorAPI.enterGrade(
                        enrollmentId, GradeComponent.END_SEM, endSemGrade, 
                        sessionManager.getCurrentInstructor().getUserId()).isSuccess();
                    if (success) savedCount++;
                    else errorCount++;
                }
                
            } catch (Exception e) {
                errorCount++;
                System.err.println("Error saving grades for enrollment " + enrollmentId + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        if (errorCount == 0) {
            showMessage("All grades saved successfully! Total: " + savedCount);
        } else {
            showMessage("Saved " + savedCount + " grades with " + errorCount + " errors.");
        }
    }
    
    
    
    
    
    private Double convertToDouble(Object value) {
        if (value == null) return null;
        if (value instanceof Double) return (Double) value;
        if (value instanceof String) {
            String str = ((String) value).trim();
            if (str.isEmpty()) return null;
            try {
                return Double.parseDouble(str);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
    
    private void computeAllFinalGrades() {
        if (selectedSection == null) {
            showError("Please select a section first.");
            return;
        }
        
        int computedCount = 0;
        int errorCount = 0;
        
        for (Enrollment enrollment : currentEnrollments) {
            InstructorAPI.ApiResult<Boolean> result = instructorAPI.computeFinalGrade(
                enrollment.getEnrollmentId(), sessionManager.getCurrentInstructor().getUserId());
            
            if (result.isSuccess()) {
                computedCount++;
            } else {
                errorCount++;
            }
        }
        
        loadEnrollmentsAndGradesForSection(selectedSection.getSectionId());
        
        if (errorCount == 0) {
            showMessage("Final grades computed for all " + computedCount + " students!");
        } else {
            showMessage("Computed final grades for " + computedCount + " students with " + errorCount + " errors.");
        }
    }

    private void exportGradesToCSV() {
        if (selectedSection == null) {
            showError("Please select a section first.");
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Grades to CSV");
        fileChooser.setSelectedFile(new File("grades_section_" + selectedSection.getSectionId() + ".csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            InstructorAPI.ApiResult<Boolean> result = 
                instructorAPI.exportGradeSheet(selectedSection.getSectionId(), 
                    fileChooser.getSelectedFile().getAbsolutePath());
            
            if (result.isSuccess()) {
                showMessage(result.getMessage());
            } else {
                showError(result.getMessage());
            }
        }
    }

    private void importGradesFromCSV() {
        if (selectedSection == null) {
            showError("Please select a section first.");
            return;
        }
        
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Import Grades from CSV");
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            showMessage("CSV import functionality will be implemented in the next version.");
        }
    }

    private void clearAllGrades() {
        if (confirmAction("Are you sure you want to clear all grades for this section? This cannot be undone.")) {
            DefaultTableModel model = (DefaultTableModel) enrollmentsTable.getModel();
            for (int row = 0; row < model.getRowCount(); row++) {
                model.setValueAt("", row, 4); 
                model.setValueAt("", row, 5);
                model.setValueAt("", row, 6); 
                model.setValueAt("", row, 7); 
            }
            showMessage("All grades cleared from the table. Remember to save changes.");
        }
    } 
    
    private static class GradeCellEditor extends DefaultCellEditor {
        private JTextField textField;
        
        public GradeCellEditor() {
            super(new JTextField());
            this.textField = (JTextField) getComponent();
            this.textField.setHorizontalAlignment(JTextField.RIGHT);
        }
        
        @Override
        public boolean stopCellEditing() {
            String text = textField.getText().trim();
            
            if (text.isEmpty()) {
                return super.stopCellEditing();
            }
            
            try {
                double grade = Double.parseDouble(text);
                if (grade < 0 || grade > 100) {
                    JOptionPane.showMessageDialog(textField, 
                        "Grade must be between 0 and 100", 
                        "Invalid Grade", 
                        JOptionPane.ERROR_MESSAGE);
                    textField.requestFocus();
                    return false;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(textField, 
                    "Please enter a valid number", 
                    "Invalid Input", 
                    JOptionPane.ERROR_MESSAGE);
                textField.requestFocus();
                return false;
            }
            
            return super.stopCellEditing();
        }
        
        @Override
        public Object getCellEditorValue() {
            String text = textField.getText().trim();
            if (text.isEmpty()) {
                return "";
            }
            try {
                return Double.parseDouble(text); 
            } catch (NumberFormatException e) {
                return "";
            }
        }
    }   
    
    
    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            
            if (value == null) {
                setText("Compute Final");
                setEnabled(true);
            } else if ("✅ Computed".equals(value.toString())) {
                setText("✅ Computed");
                setEnabled(true);
                setBackground(new Color(200, 255, 200)); 
            } else if ("Computing...".equals(value.toString())) {
                setText("Computing...");
                setEnabled(false);
                setBackground(Color.YELLOW);
            } else {
                setText("Compute Final");
                setEnabled(true);
                setBackground(isSelected ? table.getSelectionBackground() : table.getBackground());
            }
            
            return this;
        }
    } 

    private class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private int row;
        
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            this.row = row;
            button.setText("Computing...");
            isPushed = true;
            return button;
        }
        
        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
               
                computeFinalGradeForRow(row);
            }
            isPushed = false;
            return "Compute Final";
        }
    }

    private void handleActionButtonClick(int row) {
        computeFinalGradeForRow(row);
    }

    private void computeFinalGradeForRow(int row) {
        DefaultTableModel model = (DefaultTableModel) enrollmentsTable.getModel();
        int enrollmentId = (Integer) model.getValueAt(row, 0);
        
        Object quizObj = model.getValueAt(row, 5);
        Object midtermObj = model.getValueAt(row, 6);
        Object endSemObj = model.getValueAt(row, 7);
        
        Double quizGrade = convertToDouble(quizObj);
        Double midtermGrade = convertToDouble(midtermObj);
        Double endSemGrade = convertToDouble(endSemObj);
        
        if (quizGrade != null && midtermGrade != null && endSemGrade != null) {
            double finalGrade = (quizGrade * 0.20) + (midtermGrade * 0.30) + (endSemGrade * 0.50);
            String formattedFinalGrade = String.format("%.2f", finalGrade);
            model.setValueAt(formattedFinalGrade, row, 8);    
            
          
            boolean componentsSaved = saveComponentGradesToDatabase(enrollmentId, quizGrade, midtermGrade, endSemGrade);
            if (componentsSaved) {
                InstructorAPI.ApiResult<Boolean> result = instructorAPI.computeFinalGrade(
                    enrollmentId, sessionManager.getCurrentInstructor().getUserId());
                
                if (result.isSuccess()) {
                    model.setValueAt("✅ Computed", row, 9);
                    showMessage("Final grade computed and saved: " + formattedFinalGrade);
                } else {
                    showError("Failed to compute final grade: " + result.getMessage());
                }
            } else {
                showError("Failed to save component grades. Cannot compute final grade.");
            }
        } else {
            showError("Please enter all three grades (Quiz, Midterm, End Sem) first.");
        }
        
        enrollmentsTable.repaint();
    }

    
    
    private boolean saveComponentGradesToDatabase(int enrollmentId, Double quiz, Double midterm, Double endSem) {
        try {
            boolean allSaved = true;
            
            if (quiz != null) {
                ApiResult<Boolean> quizResult = instructorAPI.enterGrade(
                    enrollmentId, GradeComponent.QUIZ, quiz, 
                    sessionManager.getCurrentInstructor().getUserId());
                allSaved = allSaved && quizResult.isSuccess();
            }
            
            if (midterm != null) {
                ApiResult<Boolean> midtermResult = instructorAPI.enterGrade(
                    enrollmentId, GradeComponent.MIDTERM, midterm, 
                    sessionManager.getCurrentInstructor().getUserId());
                allSaved = allSaved && midtermResult.isSuccess();
            }
            
            if (endSem != null) {
                ApiResult<Boolean> endSemResult = instructorAPI.enterGrade(
                    enrollmentId, GradeComponent.END_SEM, endSem, 
                    sessionManager.getCurrentInstructor().getUserId());
                allSaved = allSaved && endSemResult.isSuccess();
            }
            
            return allSaved;
        } catch (Exception e) {
            System.err.println("Error saving component grades: " + e.getMessage());
            return false;
        }
    }
    
    
    private boolean saveFinalGradeToDatabase(int enrollmentId, double finalGrade) {
    	 try {
    	        InstructorAPI.ApiResult<Boolean> result = instructorAPI.computeFinalGrade(
    	            enrollmentId, sessionManager.getCurrentInstructor().getUserId());
    	        
    	        return result.isSuccess();
    	    } catch (Exception e) {
    	        System.err.println("Error saving final grade: " + e.getMessage());
    	        return false;
    	    }
	}

	private boolean saveComponentGrades(int enrollmentId, Double quiz, Double midterm, Double endSem) {
        try {
            boolean quizSaved = true, midtermSaved = true, endSemSaved = true;
            
            if (quiz != null) {
                quizSaved = instructorAPI.enterGrade(
                    enrollmentId, GradeComponent.QUIZ, quiz, 
                    sessionManager.getCurrentInstructor().getUserId()).isSuccess();
            }
            
            if (midterm != null) {
                midtermSaved = instructorAPI.enterGrade(
                    enrollmentId, GradeComponent.MIDTERM, midterm, 
                    sessionManager.getCurrentInstructor().getUserId()).isSuccess();
            }
            
            if (endSem != null) {
                endSemSaved = instructorAPI.enterGrade(
                    enrollmentId, GradeComponent.END_SEM, endSem, 
                    sessionManager.getCurrentInstructor().getUserId()).isSuccess();
            }
            
            return quizSaved && midtermSaved && endSemSaved;
        } catch (Exception e) {
            System.err.println("Error saving component grades: " + e.getMessage());
            return false;
        }
    }
    
    
}