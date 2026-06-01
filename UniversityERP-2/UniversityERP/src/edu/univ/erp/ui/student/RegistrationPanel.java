package edu.univ.erp.ui.student;

import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Course;
import edu.univ.erp.api.StudentAPI;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.data.CourseDAO;
import edu.univ.erp.data.InstructorDAO;
import edu.univ.erp.util.MessageHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class RegistrationPanel extends JPanel {
    private Student student;
    private JTable registrationTable;
    private DefaultTableModel tableModel;
    private StudentAPI studentAPI;
    private CourseDAO courseDAO;
    private InstructorDAO instructorDAO;
    private Map<String, Course> courseCache;
    private Map<Integer, String> instructorCache;
    private JLabel countLabel; 
    
    public RegistrationPanel(Student student) {
        this.student = student;
        this.studentAPI = new StudentAPI();
        this.courseDAO = new CourseDAO();
        this.instructorDAO = new InstructorDAO();
        this.courseCache = new HashMap<>();
        this.instructorCache = new HashMap<>();
        initializeUI();
        loadRegistrations();
     
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
        JButton refreshBtn = new JButton("🔄 Refresh");
        JButton dropBtn = new JButton("❌ Drop Selected Section");
        
        refreshBtn.addActionListener(e -> loadRegistrations());
        dropBtn.addActionListener(e -> dropSection());
        
        toolBar.add(refreshBtn);
        toolBar.add(Box.createHorizontalStrut(10));
        toolBar.add(dropBtn);
        
        add(toolBar, BorderLayout.NORTH);
        
        String[] columns = {"Section ID", "Course Code", "Course Title", "Credits", 
                          "Instructor", "Day", "Time", "Room", "Status", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 9; 
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 9 ? JButton.class : Object.class;
            }
        };
        
        registrationTable = new JTable(tableModel);
        registrationTable.setRowHeight(30);
        registrationTable.getColumnModel().getColumn(9).setCellRenderer(new ButtonRenderer());
        registrationTable.getColumnModel().getColumn(9).setCellEditor(new ButtonEditor(new JCheckBox()));
        
        JScrollPane scrollPane = new JScrollPane(registrationTable);
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        statsPanel.setBorder(BorderFactory.createTitledBorder("Registration Summary"));
        statsPanel.add(new JLabel("Total Registered Courses: "));
        countLabel = new JLabel("0");
        countLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
        statsPanel.add(countLabel);
        add(statsPanel, BorderLayout.SOUTH);
    }
    
    private void loadRegistrations() {
        try {
            List<Section> registrations = studentAPI.getStudentRegistrations(SessionManager.getInstance().getCurrentStudent().getUserId());
            System.out.println("hi"+registrations);
            tableModel.setRowCount(0);
            
            loadCourseDetails(registrations);
            loadInstructorDetails(registrations);
            
            for (Section section : registrations) {
                Course course = courseCache.get(section.getCourseCode());
                String courseTitle = course != null ? course.getTitle() : "Unknown Course";
                int credits = course != null ? course.getCredits() : 0;
                
                String instructorName = "TBA";
                if (section.getInstructorId() != null) {
                    instructorName = instructorCache.getOrDefault(section.getInstructorId(), "TBA");
                }
                
                Object[] row = {
                    section.getSectionId(),
                    section.getCourseCode(),
                    courseTitle,
                    credits,
                    instructorName,
                    section.getDay() != null ? section.getDay().getDisplayName() : "TBA",
                    section.getTime(),
                    section.getRoom(),
                    "REGISTERED",
                    "Drop"
                };
                tableModel.addRow(row);
            }
            
            updateStatistics(registrations.size());
            
            if (registrations.size() > 0) {
                System.out.println("Registrations loaded: " + registrations.size() + " courses");
            } else {
                System.out.println("No registrations found");
            }
            
        } catch (Exception e) {
            MessageHelper.showError(this, "Error loading registrations: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void loadCourseDetails(List<Section> sections) {
        courseCache.clear();
        for (Section section : sections) {
            String courseCode = section.getCourseCode();
            if (!courseCache.containsKey(courseCode)) {
                Course course = courseDAO.getCourseByCode(courseCode);
                if (course != null) {
                    courseCache.put(courseCode, course);
                }
            }
        }
    }
    
    private void loadInstructorDetails(List<Section> sections) {
        instructorCache.clear();
        for (Section section : sections) {
            if (section.getInstructorId() != null && !instructorCache.containsKey(section.getInstructorId())) {
                String instructorName = instructorDAO.getInstructorUsername(section.getInstructorId());
                if (instructorName != null) {
                    instructorCache.put(section.getInstructorId(), instructorName);
                }
            }
        }
    }
    
    private void dropSection() {
        int selectedRow = registrationTable.getSelectedRow();
        if (selectedRow == -1) {
            MessageHelper.showWarning(this, "Please select a section to drop");
            return;
        }
        
        int modelRow = registrationTable.convertRowIndexToModel(selectedRow);
        int sectionId = (int) tableModel.getValueAt(modelRow, 0);
        String courseCode = (String) tableModel.getValueAt(modelRow, 1);
        String courseTitle = (String) tableModel.getValueAt(modelRow, 2);
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to drop:\n" +
            courseCode + " - " + courseTitle + "\n" +
            "Section: " + sectionId + "\n" +
            "This action cannot be undone.",
            "Confirm Drop", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            try {
            	System.out.println("section id"+sectionId);
                boolean success = studentAPI.dropSection(student.getUserId(), sectionId).isSuccess();
                if (success) {
                    MessageHelper.showSuccess(this, "Successfully dropped " + courseCode + " - " + courseTitle);
                    loadRegistrations(); 
                    refreshCatalogPanel();
                } else {
                    MessageHelper.showError(this, "Failed to drop section");
                }
            } catch (Exception e) {
                MessageHelper.showError(this, "Drop failed: " + e.getMessage());
            }
        }
    }
    
    private void updateStatistics(int count) {
        if (countLabel != null) {
            countLabel.setText(String.valueOf(count));
        }
    }
    
    private void refreshCatalogPanel() {
        System.out.println("Drop successful - catalog panel should refresh");
    }
    
    private class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() { 
            setOpaque(true);
            setBackground(new Color(255, 99, 71)); 
            setForeground(Color.WHITE);
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }
    
    private class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private boolean isPushed;
        private int currentRow;
        
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setBackground(new Color(255, 99, 71));
            button.setForeground(Color.WHITE);
            button.addActionListener(e -> fireEditingStopped());
        }
        
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            button.setText(value.toString());
            isPushed = true;
            currentRow = table.convertRowIndexToModel(row);
            return button;
        }
        
        public Object getCellEditorValue() {
            if (isPushed) {
                SwingUtilities.invokeLater(() -> {
                    int sectionId = (int) tableModel.getValueAt(currentRow, 0);
                    String courseCode = (String) tableModel.getValueAt(currentRow, 1);
                    String courseTitle = (String) tableModel.getValueAt(currentRow, 2);
                    dropSpecificSection(sectionId, courseCode, courseTitle);
                });
            }
            isPushed = false;
            return "Drop";
        }
        
        private void dropSpecificSection(int sectionId, String courseCode, String courseTitle) {
            int confirm = JOptionPane.showConfirmDialog(ButtonEditor.this.getComponent(),
                "Are you sure you want to drop " + courseCode + " - " + courseTitle + "?",
                "Confirm Drop", JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                try {
                	System.out.println("section id2"+sectionId);
                    boolean success = studentAPI.dropSection(student.getUserId(), sectionId).isSuccess();
                    if (success) {
                        MessageHelper.showSuccess(button, "Successfully dropped " + courseCode + " - " + courseTitle);
                        loadRegistrations();
                        refreshCatalogPanel();
                    } else {
                        MessageHelper.showError(button, "Failed to drop section");
                    }
                } catch (Exception e) {
                    MessageHelper.showError(button, "Drop failed: " + e.getMessage());
                }
            }
        }
    }
}