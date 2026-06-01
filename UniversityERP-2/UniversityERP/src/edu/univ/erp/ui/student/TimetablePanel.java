package edu.univ.erp.ui.student;

import edu.univ.erp.domain.Student;
import edu.univ.erp.domain.Section;
import edu.univ.erp.domain.DayOfWeek;
import edu.univ.erp.api.StudentAPI;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.data.CourseDAO;
import edu.univ.erp.util.MessageHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.*;

public class TimetablePanel extends JPanel {
    private Student student;
    private StudentAPI studentAPI;
    private CourseDAO courseDAO;
    private JTable timetableTable;
    private DefaultTableModel tableModel;
    
    private final String[] timeSlots = {
        "08:00-09:30", "09:30-11:00", "11:00-12:30", 
        "12:30-14:00", "14:00-15:30", "15:30-17:00"
    };
    
    public TimetablePanel(Student currentStudent) {
        this.student = currentStudent;
        this.studentAPI = new StudentAPI();
        this.courseDAO = new CourseDAO();
        initializeUI();
        loadTimetable();
    }
    
    private void initializeUI() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("My Weekly Timetable", SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(titleLabel, BorderLayout.NORTH);
        
        String[] columns = {"Time", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        timetableTable = new JTable(tableModel);
        timetableTable.setRowHeight(60);
        timetableTable.setFont(new Font("SansSerif", Font.PLAIN, 11));
        
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(JLabel.CENTER);
        for (int i = 0; i < timetableTable.getColumnCount(); i++) {
            timetableTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }
        
        JScrollPane scrollPane = new JScrollPane(timetableTable);
        add(scrollPane, BorderLayout.CENTER);
        
        JButton refreshBtn = new JButton("🔄 Refresh Timetable");
        refreshBtn.addActionListener(e -> loadTimetable());
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(refreshBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void loadTimetable() {
        try {
      
        	
        	List<Section> registrations = studentAPI.getStudentRegistrations(SessionManager.getInstance().getCurrentUserId());
            
            tableModel.setRowCount(0);
            
            Map<String, Map<DayOfWeek, String>> timetableData = initializeTimetableData();
            
            for (Section section : registrations) {
                String courseInfo = formatCourseInfo(section);
                String timeSlot = section.getTime();
                DayOfWeek day = section.getDay();
                
                if (timetableData.containsKey(timeSlot) && day != null) {
                    timetableData.get(timeSlot).put(day, courseInfo);
                }
            }
            
            for (String timeSlot : timeSlots) {
                Map<DayOfWeek, String> dayData = timetableData.get(timeSlot);
                if (dayData == null) continue;
                
                Object[] row = new Object[6];
                row[0] = "<html><b>" + timeSlot + "</b></html>";
                row[1] = dayData.getOrDefault(DayOfWeek.MONDAY, "");
                row[2] = dayData.getOrDefault(DayOfWeek.TUESDAY, "");
                row[3] = dayData.getOrDefault(DayOfWeek.WEDNESDAY, "");
                row[4] = dayData.getOrDefault(DayOfWeek.THURSDAY, "");
                row[5] = dayData.getOrDefault(DayOfWeek.FRIDAY, "");
                
                tableModel.addRow(row);
            }
            
            applyColorCoding();
            
        } catch (Exception e) {
            MessageHelper.showError(this, "Error loading timetable: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private Map<String, Map<DayOfWeek, String>> initializeTimetableData() {
        Map<String, Map<DayOfWeek, String>> data = new HashMap<>();
        for (String timeSlot : timeSlots) {
            Map<DayOfWeek, String> dayMap = new HashMap<>();
            for (DayOfWeek day : DayOfWeek.values()) {
                if (day != DayOfWeek.SATURDAY && day != DayOfWeek.SUNDAY) {
                    dayMap.put(day, "");
                }
            }
            data.put(timeSlot, dayMap);
        }
        return data;
    }
    
    private String formatCourseInfo(Section section) {
        String courseCode = section.getCourseCode();
        String room = section.getRoom();
        
        return String.format("<html><center><b>%s</b><br/>%s<br/>%s</center></html>", 
                           courseCode, getCourseTitle(courseCode), room);
    }
    
    private String getCourseTitle(String courseCode) {
        try {
            var course = courseDAO.getCourseByCode(courseCode);
            return course != null ? course.getTitle() : "Unknown Course";
        } catch (Exception e) {
            return "Unknown Course";
        }
    }
    
    private void applyColorCoding() {
        timetableTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            private final Color[] colors = {
                new Color(220, 240, 255), 
                new Color(255, 240, 220), 
                new Color(220, 255, 220), 
                new Color(255, 220, 220), 
                new Color(240, 220, 255), 
                new Color(255, 255, 220) 
            };
            
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                
                if (column > 0 && value != null && !value.toString().isEmpty()) {
                    int colorIndex = (row % colors.length);
                    c.setBackground(colors[colorIndex]);
                    c.setForeground(Color.BLACK);
                } else {
                    c.setBackground(Color.WHITE);
                    c.setForeground(Color.BLACK);
                }
                
                setHorizontalAlignment(JLabel.CENTER);
                return c;
            }
        });
    }
}