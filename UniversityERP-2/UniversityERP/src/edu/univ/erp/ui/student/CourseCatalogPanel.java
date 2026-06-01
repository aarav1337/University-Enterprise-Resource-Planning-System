package edu.univ.erp.ui.student;

import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.Course;
import edu.univ.erp.api.CatalogAPI;
import edu.univ.erp.api.StudentAPI;
import edu.univ.erp.api.StudentAPI.ApiResult;
import edu.univ.erp.data.CourseDAO;
import edu.univ.erp.data.InstructorDAO;
import edu.univ.erp.util.MessageHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class CourseCatalogPanel extends JPanel {
    private Student student;
    private JTable courseTable;
    private DefaultTableModel tableModel;
    private CatalogAPI catalogAPI;
    private StudentAPI studentAPI;
    private CourseDAO courseDAO;
    private InstructorDAO instructorDAO;
    private Map<String, Course> courseCache;
    private Map<Integer, String> instructorCache;
    private boolean isRegistering = false; 
    
    public CourseCatalogPanel(Student student) {
        this.student = student;
        this.catalogAPI = new CatalogAPI();
        this.studentAPI = new StudentAPI();
        this.courseDAO = new CourseDAO();
        this.instructorDAO = new InstructorDAO();
        this.courseCache = new HashMap<>();
        this.instructorCache = new HashMap<>();
        initializeUI();
        loadCourseCatalog();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        
        JButton refreshBtn = new JButton("🔄 Refresh");
        JButton registerBtn = new JButton("📝 Register for Selected Section");
        
        refreshBtn.addActionListener(e -> loadCourseCatalog());
        registerBtn.addActionListener(e -> registerForSelectedSection());
        
        toolBar.add(refreshBtn);
        toolBar.add(Box.createHorizontalStrut(10));
        toolBar.add(registerBtn);
        
        add(toolBar, BorderLayout.NORTH);
        
        String[] columns = {
            "Section ID", "Course Code", "Course Title", "Credits", 
            "Instructor", "Day", "Time", "Room", "Capacity", "Enrolled", "Available", "Actions"
        };
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 11; 
            }
            
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 11 ? JButton.class : Object.class;
            }
        };
        
        courseTable = new JTable(tableModel);
        courseTable.setRowHeight(30);
        courseTable.getColumnModel().getColumn(11).setCellRenderer(new ButtonRenderer());
        courseTable.getColumnModel().getColumn(11).setCellEditor(new ButtonEditor(new JCheckBox()));
        
        courseTable.setAutoCreateRowSorter(true);
        
        JScrollPane scrollPane = new JScrollPane(courseTable);
        scrollPane.setPreferredSize(new Dimension(1000, 400));
        add(scrollPane, BorderLayout.CENTER);
        
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Information"));
        infoPanel.add(new JLabel("Click Register button to enroll in a section | Green = Available | Red = Full"));
        add(infoPanel, BorderLayout.SOUTH);
    }
    
    private void loadCourseCatalog() {
        try {
            List<Section> sections = catalogAPI.getAllSections().getData();
            tableModel.setRowCount(0);
            
            loadCourseDetails(sections);
            loadInstructorDetails(sections);
            
            for (Section section : sections) {
                int available = section.getAvailableSeats();
                boolean isAvailable = section.hasAvailableSeats();
                
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
                    section.getCapacity(),
                    section.getEnrolledCount(), 
                    available,
                    isAvailable ? "Register" : "Full"
                };
                tableModel.addRow(row);
            }
            
            MessageHelper.showInfo(this, "Loaded " + sections.size() + " sections");
            
        } catch (Exception e) {
            MessageHelper.showError(this, "Error loading course catalog: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void registerForSelectedSection() {
        int selectedRow = courseTable.getSelectedRow();
        if (selectedRow == -1) {
            MessageHelper.showWarning(this, "Please select a section to register");
            return;
        }
        
        int modelRow = courseTable.convertRowIndexToModel(selectedRow);
        int sectionId = (int) tableModel.getValueAt(modelRow, 0);
        String courseCode = (String) tableModel.getValueAt(modelRow, 1);
        String courseTitle = (String) tableModel.getValueAt(modelRow, 2);
        
        performRegistration(sectionId, courseCode, courseTitle, modelRow);
    }
    
    private void performRegistration(int sectionId, String courseCode, String courseTitle, int modelRow) {
        if (isRegistering) {
            return;
        }
        
        int available = (int) tableModel.getValueAt(modelRow, 10);
        
        if (available <= 0) {
            MessageHelper.showWarning(this, "Section full. Cannot register.");
            return;
        }
        
        int confirm = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to register for:\n" +
            "Course: " + courseCode + " - " + courseTitle + "\n" +
            "Section: " + sectionId + "\n" +
            "Instructor: " + tableModel.getValueAt(modelRow, 4) + "\n" +
            "Day: " + tableModel.getValueAt(modelRow, 5) +
            " | Time: " + tableModel.getValueAt(modelRow, 6),
            "Confirm Registration", JOptionPane.YES_NO_OPTION);
            
        if (confirm == JOptionPane.YES_OPTION) {
            isRegistering = true;
            try {
                ApiResult<Boolean> result = studentAPI.registerForSection(student.getUserId(), sectionId);
                
                if (result.isSuccess()) {
                    MessageHelper.showSuccess(this, "Successfully registered for " + courseCode + " - " + courseTitle);
                    loadCourseCatalog(); 
                    refreshOtherPanels();
                } else {
                    String errorMessage = result.getMessage() != null ? result.getMessage() : "Registration failed";
                    MessageHelper.showError(this, errorMessage);
                }
            } catch (Exception e) {
                MessageHelper.showError(this, "Registration failed: " + e.getMessage());
            } finally {
                isRegistering = false;
            }
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
    
    private void refreshOtherPanels() {
        System.out.println("Registration successful - other panels should refresh");
    }
    
    private class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }
        
        public Component getTableCellRendererComponent(JTable table, Object value,
                boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            
            if ("Register".equals(value)) {
                setBackground(new Color(144, 238, 144)); 
                setEnabled(true);
            } else if ("Full".equals(value)) {
                setBackground(new Color(255, 182, 193)); 
                setEnabled(false);
            } else {
                setBackground(UIManager.getColor("Button.background"));
                setEnabled(true);
            }
            
            return this;
        }
    }
    
    private class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private int currentRow;
        
        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }
        
        public Component getTableCellEditorComponent(JTable table, Object value,
                boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
            button.setText(label);
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
                    String buttonText = (String) tableModel.getValueAt(currentRow, 11);
                    
                    if ("Register".equals(buttonText)) {
                        performRegistration(sectionId, courseCode, courseTitle, currentRow);
                    }
                });
            }
            isPushed = false;
            return label;
        }
    }
}