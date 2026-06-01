package edu.univ.erp.ui.admin;

import edu.univ.erp.api.AdminAPI;
import edu.univ.erp.api.AdminAPI.ApiResult;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.*;
import edu.univ.erp.ui.common.MainFrame;
import edu.univ.erp.ui.common.TableHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

public class AdminDashboard extends MainFrame {
    private JTabbedPane tabbedPane;
    private AdminAPI adminAPI;
    private SessionManager sessionManager;
    
    private JTable usersTable;
    private JTable coursesTable;
    private JTable sectionsTable;
    
    private List<User> currentUsers;
    private List<Course> currentCourses;
    private List<Section> currentSections;
    private List<Instructor> currentInstructors;
    
    public AdminDashboard() {
        super("Admin Dashboard");
        this.adminAPI = new AdminAPI();
        this.sessionManager = SessionManager.getInstance();

    }
    
    @Override
    protected void initializeUI() {
        this.adminAPI = new AdminAPI();
        this.sessionManager = SessionManager.getInstance();
        
        super.initializeUI();
        
        setSize(1200, 800);
        setLocationRelativeTo(null);
        
        loadAdminData();
    }
    
    @Override
    protected JPanel createContentPanel() {
        JPanel contentPanel = new JPanel(new BorderLayout());
        
        tabbedPane = new JTabbedPane();
        
        tabbedPane.addTab("User Management", createUserManagementPanel());
        tabbedPane.addTab("Course Management", createCourseManagementPanel());
        tabbedPane.addTab("Section Management", createSectionManagementPanel());
        tabbedPane.addTab("System Settings", createSystemSettingsPanel());
        
        contentPanel.add(tabbedPane, BorderLayout.CENTER);
        
        return contentPanel;
    }
    
    
    private void loadAdminData() {
        if (adminAPI == null) {
            System.out.println("DEBUG: adminAPI is null in loadAdminData");
            return;
        }
        
        ApiResult<List<User>> usersResult = adminAPI.getAllUsers();
        System.out.println(usersResult.getData()+" "+usersResult.isSuccess());
        
        if (usersResult.isSuccess()) {
            currentUsers = usersResult.getData();
        } 
        ApiResult<List<Course>> coursesResult = adminAPI.getAllCourses();
        if (coursesResult.isSuccess()) {
            currentCourses = coursesResult.getData();
        } 
        
        ApiResult<List<Section>> sectionsResult = adminAPI.getAllSections();
        if (sectionsResult.isSuccess()) {
            currentSections = sectionsResult.getData();
        } 
     
        ApiResult<List<Instructor>> instructorsResult = adminAPI.getAllInstructors();
        if (instructorsResult.isSuccess()) {
            currentInstructors = instructorsResult.getData();
        }
    }
    
    
    private void showUserManagementDialogDirect(int userId, String username, UserRole role) {
        
        boolean profileExists = false;
        boolean canCreateProfile = false;
        
        if (role == UserRole.STUDENT) {
            ApiResult<Boolean> profileResult = adminAPI.studentProfileExists(userId);
            if (profileResult.isSuccess()) {
                profileExists = Boolean.TRUE.equals(profileResult.getData());
                canCreateProfile = !profileExists;
            }
        } else if (role == UserRole.INSTRUCTOR) {
            ApiResult<Boolean> profileResult = adminAPI.instructorProfileExists(userId);
            if (profileResult.isSuccess()) {
                profileExists = Boolean.TRUE.equals(profileResult.getData());
                canCreateProfile = !profileExists;
            }
        }
        
        
        String profileStatus = profileExists ? "✅ Profile Created" : "❌ No Profile";
        String message = String.format(
            "User Management for: %s\n" +
            "Role: %s\n" +
            "User ID: %d\n" +
            "Profile Status: %s\n\n" +
            "What would you like to do?", 
            username, role.getDisplayName(), userId, profileStatus
        );
        
        java.util.List<String> optionsList = new java.util.ArrayList<>();
        optionsList.add("View Details");
        optionsList.add("Reset Password");
        optionsList.add("Lock/Unlock");
        
        if (canCreateProfile && (role == UserRole.STUDENT || role == UserRole.INSTRUCTOR)) {
            optionsList.add("Create Profile");
        }
        
        optionsList.add("Cancel");
        
        String[] options = optionsList.toArray(new String[0]);
        
        int choice = JOptionPane.showOptionDialog(
            this, 
            message, 
            "Manage User", 
            JOptionPane.DEFAULT_OPTION, 
            JOptionPane.INFORMATION_MESSAGE, 
            null, 
            options, 
            options[0]
        );
        
        
        switch (choice) {
            case 0: 
            	showUserDetails(userId, username, role);
                break;
            case 1: 
            	resetUserPassword(userId, username);
                break;
            case 2: 
            	toggleUserLock(userId, username);
                break;
            case 3: 
                if (options[choice].equals("Create Profile")) {
                	createUserProfile(userId, username, role);
                }
                break;
        }
    }

  
    private void showUserDetails(int userId, String username, UserRole role) {
        ApiResult<List<User>> usersResult = adminAPI.getAllUsers();
        if (usersResult.isSuccess()) {
            User user = usersResult.getData().stream()
                .filter(u -> u.getUserId() == userId)
                .findFirst()
                .orElse(null);
            
            if (user != null) {
                String details = String.format(
                    "User Details:\n\n" +
                    "User ID: %d\n" +
                    "Username: %s\n" +
                    "Role: %s\n" +
                    "Status: %s\n" +
                    "Last Login: %s\n" +
                    "Failed Attempts: %d\n" +
                    "Created: (Not stored in current schema)",
                    user.getUserId(),
                    user.getUsername(),
                    user.getRole().getDisplayName(),
                    user.getStatus().getDisplayName(),
                    user.getLastLogin() != null ? user.getLastLogin() : "Never",
                    user.getFailedLoginAttempts()
                );
                
                JOptionPane.showMessageDialog(
                    AdminDashboard.this, 
                    details, 
                    "User Details", 
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        }
    }

    
    
    
    private void createUserProfile(int userId, String username, UserRole role) {
        ApiResult<User> userResult = adminAPI.getUserById(userId);
        if (userResult.isSuccess() && userResult.getData() != null) {
            User user = userResult.getData();
            if (role == UserRole.STUDENT) {
                promptStudentProfileCreation(user);
            } else if (role == UserRole.INSTRUCTOR) {
                promptInstructorProfileCreation(user);
            }
        } else {
            JOptionPane.showMessageDialog(
                AdminDashboard.this,
                "Unable to retrieve user information. Please try again.",
                "Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    
    
  
    private void resetUserPassword(int userId, String username) {
        String newPassword = JOptionPane.showInputDialog(
            AdminDashboard.this, 
            "Enter new password for " + username + ":", 
            "Reset Password", 
            JOptionPane.QUESTION_MESSAGE
        );
        
        if (newPassword != null && !newPassword.trim().isEmpty()) {
            if (newPassword.length() < 6) {
                JOptionPane.showMessageDialog(
                    AdminDashboard.this,
                    "Password must be at least 6 characters long",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
                return;
            }
            
            ApiResult<Boolean> result = adminAPI.resetUserPassword(userId, newPassword);
            if (result.isSuccess()) {
                JOptionPane.showMessageDialog(
                    AdminDashboard.this,
                    "Password reset successfully for " + username,
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                JOptionPane.showMessageDialog(
                    AdminDashboard.this,
                    result.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    private void toggleUserLock(int userId, String username) {
        ApiResult<List<User>> usersResult = adminAPI.getAllUsers();
        if (usersResult.isSuccess()) {
            User user = usersResult.getData().stream()
                .filter(u -> u.getUserId() == userId)
                .findFirst()
                .orElse(null);
            
            if (user != null) {
                if (user.isLocked()) {
                    int confirm = JOptionPane.showConfirmDialog(
                        AdminDashboard.this,
                        "Unlock user " + username + "?",
                        "Confirm Unlock",
                        JOptionPane.YES_NO_OPTION
                    );
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        ApiResult<Boolean> result = adminAPI.unlockUser(userId);
                        if (result.isSuccess()) {
                            JOptionPane.showMessageDialog(
                                AdminDashboard.this,
                                "User unlocked successfully!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                            );
                            refreshUsersTable();
                        } else {
                            JOptionPane.showMessageDialog(
                                AdminDashboard.this,
                                result.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }
                } else {
                    int confirm = JOptionPane.showConfirmDialog(
                        AdminDashboard.this,
                        "Lock user " + username + "?",
                        "Confirm Lock",
                        JOptionPane.YES_NO_OPTION
                    );
                    
                    if (confirm == JOptionPane.YES_OPTION) {
                        ApiResult<Boolean> result = adminAPI.lockUser(userId);
                        if (result.isSuccess()) {
                            JOptionPane.showMessageDialog(
                                AdminDashboard.this,
                                "User locked successfully!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE
                            );
                            refreshUsersTable();
                        } else {
                            JOptionPane.showMessageDialog(
                                AdminDashboard.this,
                                result.getMessage(),
                                "Error",
                                JOptionPane.ERROR_MESSAGE
                            );
                        }
                    }
                }
            }
        }
    }

    
    
 // User Management Panel
    
    private JPanel createUserManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
   
        JLabel titleLabel = new JLabel("User Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel formPanel = createUserFormPanel();
        panel.add(formPanel, BorderLayout.NORTH);
        
        String[] userColumns = {"User ID", "Username", "Role", "Status", "Last Login", "Failed Attempts", "Actions"};
        DefaultTableModel userModel = new DefaultTableModel(userColumns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; 
            }
        };
        
        usersTable = new JTable(userModel);
        
        usersTable.setAutoCreateRowSorter(true);
        
        usersTable.setRowHeight(30);
        usersTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        usersTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        usersTable.setFont(new Font("SansSerif", Font.PLAIN, 11));
        usersTable.setFillsViewportHeight(true);
        
        usersTable.getColumnModel().getColumn(6).setPreferredWidth(120);
        
        usersTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = usersTable.rowAtPoint(evt.getPoint());
                int col = usersTable.columnAtPoint(evt.getPoint());
                
                System.out.println("DEBUG: Clicked on row=" + row + ", col=" + col);
                
                if (row >= 0 && col == 6) {
                    System.out.println("DEBUG: Actions column clicked!");
                    
                    int modelRow = usersTable.convertRowIndexToModel(row);
                    int userId = (Integer) usersTable.getModel().getValueAt(modelRow, 0);
                    String username = (String) usersTable.getModel().getValueAt(modelRow, 1);
                    String roleStr = (String) usersTable.getModel().getValueAt(modelRow, 2);
                    
                    System.out.println("DEBUG: User ID: " + userId + ", Username: " + username + ", Role: " + roleStr);
                    
                    try {
                        UserRole role = UserRole.valueOf(roleStr.toUpperCase());
                        
                        showUserManagementDialogDirect(userId, username, role);
                        
                    } catch (IllegalArgumentException e) {
                        System.out.println("DEBUG: Error converting role: " + e.getMessage());
                        showError("Invalid user role: " + roleStr);
                    }
                }
            }
        });
        
        JScrollPane usersScroll = new JScrollPane(usersTable);
        usersScroll.setPreferredSize(new Dimension(800, 400));
        panel.add(usersScroll, BorderLayout.CENTER);
        
        JPanel controlPanel = createUserControlPanel();
        panel.add(controlPanel, BorderLayout.SOUTH);
        
        refreshUsersTable();
        
        return panel;
    }
    
    
    
    private JPanel createUserFormPanel() {
        JPanel formPanel = new JPanel(new GridLayout(2, 4, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Create New User"));
        
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JComboBox<UserRole> roleCombo = new JComboBox<>(UserRole.values());
        JButton createUserBtn = new JButton("Create User");
        
        formPanel.add(new JLabel("Username:"));
        formPanel.add(new JLabel("Password:"));
        formPanel.add(new JLabel("Role:"));
        formPanel.add(new JLabel(""));
        
        formPanel.add(usernameField);
        formPanel.add(passwordField);
        formPanel.add(roleCombo);
        formPanel.add(createUserBtn);
        
        createUserBtn.addActionListener(e -> {
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            UserRole role = (UserRole) roleCombo.getSelectedItem();
            
            if (username.isEmpty() || password.isEmpty()) {
                showError("Please fill in all fields");
                return;
            }
            
            if (password.length() < 6) {
                showError("Password must be at least 6 characters long");
                return;
            }
            
            ApiResult<User> result = adminAPI.createUser(username, password, role);
            if (result.isSuccess()) {
                showMessage("User created successfully in Auth DB!");
                usernameField.setText("");
                passwordField.setText("");
                refreshUsersTable();
                
                if (role == UserRole.STUDENT || role == UserRole.INSTRUCTOR) {
                    promptProfileCreation(result.getData());
                }
            } else {
                showError("Failed to create user: " + result.getMessage());
            }
        });
        
        return formPanel;
    }
    
    
    private JPanel createUserControlPanel() {
        JPanel controlPanel = new JPanel(new FlowLayout());
        
        JButton refreshBtn = new JButton("Refresh Users");
        JButton lockSelectedBtn = new JButton("Lock Selected");
        JButton unlockSelectedBtn = new JButton("Unlock Selected");
        JButton resetPasswordBtn = new JButton("Reset Password");
        
        refreshBtn.addActionListener(e -> refreshUsersTable());
        
        lockSelectedBtn.addActionListener(e -> {
            int selectedRow = usersTable.getSelectedRow();
            if (selectedRow == -1) {
                showError("Please select a user first");
                return;
            }
            
            int modelRow = usersTable.convertRowIndexToModel(selectedRow);
            int userId = (Integer) usersTable.getModel().getValueAt(modelRow, 0);
            String username = (String) usersTable.getModel().getValueAt(modelRow, 1);
            
            if (confirmAction("Are you sure you want to lock user: " + username + "?")) {
                ApiResult<Boolean> result = adminAPI.lockUser(userId);
                if (result.isSuccess()) {
                    showMessage("User locked successfully!");
                    refreshUsersTable();
                } else {
                    showError(result.getMessage());
                }
            }
        });
        
        unlockSelectedBtn.addActionListener(e -> {
            int selectedRow = usersTable.getSelectedRow();
            if (selectedRow == -1) {
                showError("Please select a user first");
                return;
            }
            
            int modelRow = usersTable.convertRowIndexToModel(selectedRow);
            int userId = (Integer) usersTable.getModel().getValueAt(modelRow, 0);
            String username = (String) usersTable.getModel().getValueAt(modelRow, 1);
            
            if (confirmAction("Are you sure you want to unlock user: " + username + "?")) {
                ApiResult<Boolean> result = adminAPI.unlockUser(userId);
                if (result.isSuccess()) {
                    showMessage("User unlocked successfully!");
                    refreshUsersTable();
                } else {
                    showError(result.getMessage());
                }
            }
        });
        
        resetPasswordBtn.addActionListener(e -> {
            int selectedRow = usersTable.getSelectedRow();
            if (selectedRow == -1) {
                showError("Please select a user first");
                return;
            }
            
            int modelRow = usersTable.convertRowIndexToModel(selectedRow);
            int userId = (Integer) usersTable.getModel().getValueAt(modelRow, 0);
            String username = (String) usersTable.getModel().getValueAt(modelRow, 1);
            
            String newPassword = JOptionPane.showInputDialog(this, 
                "Enter new password for " + username + ":", "Reset Password", JOptionPane.QUESTION_MESSAGE);
            
            if (newPassword != null && !newPassword.trim().isEmpty()) {
                if (newPassword.length() < 6) {
                    showError("Password must be at least 6 characters long");
                    return;
                }
                
                ApiResult<Boolean> result = adminAPI.resetUserPassword(userId, newPassword);
                if (result.isSuccess()) {
                    showMessage("Password reset successfully!");
                } else {
                    showError(result.getMessage());
                }
            }
        });
        
        controlPanel.add(refreshBtn);
        controlPanel.add(lockSelectedBtn);
        controlPanel.add(unlockSelectedBtn);
        controlPanel.add(resetPasswordBtn);
        
        return controlPanel;
    }
    
    
    
    private void promptProfileCreation(User user) {
        if (user.getRole() == UserRole.STUDENT) {
            promptStudentProfileCreation(user);
        } else if (user.getRole() == UserRole.INSTRUCTOR) {
            promptInstructorProfileCreation(user);
        }
    }
    
    private void promptStudentProfileCreation(User user) {
        JPanel panel = new JPanel(new GridLayout(4, 2, 5, 5));
        
        JTextField rollNoField = new JTextField();
        JTextField programField = new JTextField();
        JSpinner yearSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 5, 1));
        
        panel.add(new JLabel("Roll Number:"));
        panel.add(rollNoField);
        panel.add(new JLabel("Program:"));
        panel.add(programField);
        panel.add(new JLabel("Year:"));
        panel.add(yearSpinner);
        panel.add(new JLabel(""));
        panel.add(new JLabel(""));
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Create Student Profile for " + user.getUsername(), 
            JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            String rollNo = rollNoField.getText().trim();
            String program = programField.getText().trim();
            int year = (Integer) yearSpinner.getValue();
            
            if (rollNo.isEmpty() || program.isEmpty()) {
                showError("Please fill in all fields");
                return;
            }
            
            ApiResult<Boolean> profileResult = adminAPI.createStudentProfile(user.getUserId(), rollNo, program, year);
            if (profileResult.isSuccess()) {
                showMessage("Student profile created successfully!");
            } else {
                showError("Failed to create student profile: " + profileResult.getMessage());
            }
        }
    }
    
    private void promptInstructorProfileCreation(User user) {
        JPanel panel = new JPanel(new GridLayout(2, 2, 5, 5));
        
        JTextField departmentField = new JTextField();
        
        panel.add(new JLabel("Department:"));
        panel.add(departmentField);
        panel.add(new JLabel(""));
        panel.add(new JLabel(""));
        
        int result = JOptionPane.showConfirmDialog(this, panel, 
            "Create Instructor Profile for " + user.getUsername(), 
            JOptionPane.OK_CANCEL_OPTION);
        
        if (result == JOptionPane.OK_OPTION) {
            String department = departmentField.getText().trim();
            
            if (department.isEmpty()) {
                showError("Please enter department");
                return;
            }
            
            ApiResult<Boolean> profileResult = adminAPI.createInstructorProfile(user.getUserId(), department);
            if (profileResult.isSuccess()) {
                showMessage("Instructor profile created successfully!");
            } else {
                showError("Failed to create instructor profile: " + profileResult.getMessage());
            }
        }
    }
    
    private void refreshUsersTable() {
        if (usersTable == null) return;
        
        DefaultTableModel model = (DefaultTableModel) usersTable.getModel();
        model.setRowCount(0);
        
        ApiResult<List<User>> result = adminAPI.getAllUsers();
        if (result.isSuccess() && result.getData() != null) {
            for (User user : result.getData()) {
                String profileStatus = "N/A";
                if (user.getRole() == UserRole.STUDENT) {
                    ApiResult<Boolean> profileResult = adminAPI.studentProfileExists(user.getUserId());
                    profileStatus = (profileResult.isSuccess() && Boolean.TRUE.equals(profileResult.getData())) ? "✅" : "❌";
                } else if (user.getRole() == UserRole.INSTRUCTOR) {
                    ApiResult<Boolean> profileResult = adminAPI.instructorProfileExists(user.getUserId());
                    profileStatus = (profileResult.isSuccess() && Boolean.TRUE.equals(profileResult.getData())) ? "✅" : "❌";
                }
                
                model.addRow(new Object[]{
                    user.getUserId(),
                    user.getUsername(),
                    user.getRole().getDisplayName(),
                    user.getStatus().getDisplayName(),
                    user.getLastLogin() != null ? user.getLastLogin() : "Never",
                    user.getFailedLoginAttempts(),
                    "Manage (" + profileStatus + ")"
                });
            }
        }
    }
    
    // Course Management Panel
    private JPanel createCourseManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("Course Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel formPanel = createCourseFormPanel();
        panel.add(formPanel, BorderLayout.NORTH);
        
        String[] courseColumns = {"Course Code", "Title", "Credits"};
        DefaultTableModel courseModel = TableHelper.createTableModel(courseColumns);
        coursesTable = TableHelper.createSortableTable(courseModel);
        
        JScrollPane coursesScroll = TableHelper.createScrollPaneWithTable(coursesTable);
        panel.add(coursesScroll, BorderLayout.CENTER);
        
        JButton refreshBtn = new JButton("Refresh Courses");
        refreshBtn.addActionListener(e -> refreshCoursesTable());
        panel.add(refreshBtn, BorderLayout.SOUTH);
        
        refreshCoursesTable();
        
        return panel;
    }
    
    private JPanel createCourseFormPanel() {
        JPanel formPanel = new JPanel(new GridLayout(2, 4, 5, 5));
        formPanel.setBorder(BorderFactory.createTitledBorder("Create New Course"));
        
        JTextField codeField = new JTextField();
        JTextField titleField = new JTextField();
        JSpinner creditsSpinner = new JSpinner(new SpinnerNumberModel(3, 1, 5, 1));
        JButton createCourseBtn = new JButton("Create Course");
        
        formPanel.add(new JLabel("Course Code:"));
        formPanel.add(new JLabel("Title:"));
        formPanel.add(new JLabel("Credits:"));
        formPanel.add(new JLabel(""));
        
        formPanel.add(codeField);
        formPanel.add(titleField);
        formPanel.add(creditsSpinner);
        formPanel.add(createCourseBtn);
        
        createCourseBtn.addActionListener(e -> {
            String code = codeField.getText().trim().toUpperCase();
            String title = titleField.getText().trim();
            int credits = (Integer) creditsSpinner.getValue();
            
            if (code.isEmpty() || title.isEmpty()) {
                showError("Please fill in all fields");
                return;
            }
            
            ApiResult<Boolean> result = adminAPI.createCourse(code, title, credits);
            if (result.isSuccess()) {
                showMessage("Course created successfully!");
                codeField.setText("");
                titleField.setText("");
                refreshCoursesTable();
            } else {
                showError(result.getMessage());
            }
        });
        
        return formPanel;
    }
    
    private void refreshCoursesTable() {
        if (coursesTable == null) return;
        
        DefaultTableModel model = (DefaultTableModel) coursesTable.getModel();
        model.setRowCount(0);
        
        ApiResult<List<Course>> result = adminAPI.getAllCourses();
        if (result.isSuccess() && result.getData() != null) {
            for (Course course : result.getData()) {
                model.addRow(new Object[]{
                    course.getCode(),
                    course.getTitle(),
                    course.getCredits()
                });
            }
        }
    }
    
    // Section Management Panel
    
    
    
    private JPanel createSectionFormPanel() {
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createTitledBorder("Create New Section"));
        formPanel.setPreferredSize(new Dimension(800, 200));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        ApiResult<List<Course>> coursesResult = adminAPI.getAllCourses();
        ApiResult<List<Instructor>> instructorsResult = adminAPI.getAllInstructors();
        
        JComboBox<String> courseCombo = new JComboBox<>();
        JComboBox<String> instructorCombo = new JComboBox<>();
        JComboBox<DayOfWeek> dayCombo = new JComboBox<>(DayOfWeek.values());
        JTextField timeField = new JTextField(10);
        JTextField roomField = new JTextField(10);
        JSpinner capacitySpinner = new JSpinner(new SpinnerNumberModel(30, 1, 100, 1));
        JComboBox<Semester> semesterCombo = new JComboBox<>(Semester.values());
        JSpinner yearSpinner = new JSpinner(new SpinnerNumberModel(2024, 2024, 2030, 1));
        JButton createSectionBtn = new JButton("Create Section");
        
        final List<Instructor> instructorList = new ArrayList<>();
        
        courseCombo.addItem("Select Course");
        if (coursesResult.isSuccess() && coursesResult.getData() != null) {
            for (Course course : coursesResult.getData()) {
                courseCombo.addItem(course.getCode() + " - " + course.getTitle());
            }
        }
        
        instructorCombo.addItem("No Instructor");
        if (instructorsResult.isSuccess() && instructorsResult.getData() != null) {
            instructorList.addAll(instructorsResult.getData()); // Store instructors for ID lookup
            
            for (Instructor instructor : instructorsResult.getData()) {
                ApiResult<User> userResult = adminAPI.getUserById(instructor.getUserId());
                String displayName;
                if (userResult.isSuccess() && userResult.getData() != null) {
                    displayName = userResult.getData().getUsername() + " (" + instructor.getDepartment() + ")";
                } else {
                    displayName = "Instructor " + instructor.getUserId() + " (" + instructor.getDepartment() + ")";
                }
                instructorCombo.addItem(displayName);
            }
        }
        
        gbc.gridy = 0;
        
        gbc.gridx = 0;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("Course*:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(courseCombo, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("Instructor:"), gbc);
        
        gbc.gridx = 3;
        gbc.weightx = 0.7;
        formPanel.add(instructorCombo, gbc);
        
        gbc.gridy = 1;
        
        gbc.gridx = 0;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("Day*:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(dayCombo, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("Time*:"), gbc);
        
        gbc.gridx = 3;
        gbc.weightx = 0.7;
        formPanel.add(timeField, gbc);
        
        gbc.gridy = 2;
        
        gbc.gridx = 0;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("Room*:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(roomField, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("Capacity*:"), gbc);
        
        gbc.gridx = 3;
        gbc.weightx = 0.7;
        formPanel.add(capacitySpinner, gbc);
        
        gbc.gridy = 3;
        
        gbc.gridx = 0;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("Semester*:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 0.7;
        formPanel.add(semesterCombo, gbc);
        
        gbc.gridx = 2;
        gbc.weightx = 0.3;
        formPanel.add(new JLabel("Year*:"), gbc);
        
        gbc.gridx = 3;
        gbc.weightx = 0.7;
        formPanel.add(yearSpinner, gbc);
        
        gbc.gridy = 4;
        gbc.gridx = 0;
        gbc.gridwidth = 4;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(createSectionBtn, gbc);
        
        timeField.setText("09:00-10:30");
        roomField.setText("Room 101");
        
        createSectionBtn.addActionListener(e -> {
            if (courseCombo.getSelectedIndex() == 0) {
                showError("Please select a course");
                return;
            }
            
            String courseSelection = (String) courseCombo.getSelectedItem();
            int instructorIndex = instructorCombo.getSelectedIndex();
            DayOfWeek day = (DayOfWeek) dayCombo.getSelectedItem();
            String time = timeField.getText().trim();
            String room = roomField.getText().trim();
            int capacity = (Integer) capacitySpinner.getValue();
            Semester semester = (Semester) semesterCombo.getSelectedItem();
            int year = (Integer) yearSpinner.getValue();
            
            if (time.isEmpty() || room.isEmpty()) {
                showError("Please fill in all required fields (marked with *)");
                return;
            }
            
            String courseCode = courseSelection.split(" - ")[0];
            
            Integer instructorId = null;
            if (instructorIndex > 0) {
                int actualIndex = instructorIndex - 1;
                if (actualIndex >= 0 && actualIndex < instructorList.size()) {
                    instructorId = instructorList.get(actualIndex).getUserId();
                }
            }
            
            System.out.println("DEBUG: Creating section with - Course: " + courseCode + 
                              ", Instructor ID: " + instructorId + ", Day: " + day + 
                              ", Time: " + time + ", Room: " + room);
            
            ApiResult<Boolean> result = adminAPI.createSection(courseCode, instructorId, day, time, room, capacity, semester, year);
            if (result.isSuccess()) {
                showMessage("Section created successfully!");
                clearFormFields(courseCombo, instructorCombo, dayCombo, timeField, roomField, 
                              capacitySpinner, semesterCombo, yearSpinner);
                refreshSectionsTable();
            } else {
                showError("Failed to create section: " + result.getMessage());
            }
        });
        
        return formPanel;
    }

    private void clearFormFields(JComboBox<String> courseCombo, JComboBox<String> instructorCombo,
                               JComboBox<DayOfWeek> dayCombo, JTextField timeField, JTextField roomField,
                               JSpinner capacitySpinner, JComboBox<Semester> semesterCombo, JSpinner yearSpinner) {
        courseCombo.setSelectedIndex(0);
        instructorCombo.setSelectedIndex(0);
        dayCombo.setSelectedIndex(0);
        timeField.setText("09:00-10:30");
        roomField.setText("Room 101");
        capacitySpinner.setValue(30);
        semesterCombo.setSelectedIndex(0);
        yearSpinner.setValue(2024);
    }

    private JPanel createSectionManagementPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("Section Management", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel formPanel = createSectionFormPanel();
        panel.add(formPanel, BorderLayout.NORTH);
        
        String[] sectionColumns = {
            "Section ID", "Course Code", "Course Title", "Instructor", 
            "Day", "Time", "Room", "Capacity", "Enrolled", 
            "Available", "Semester", "Year"
        };
        
        DefaultTableModel sectionModel = TableHelper.createTableModel(sectionColumns);
        sectionsTable = TableHelper.createSortableTable(sectionModel);
        
        sectionsTable.setRowHeight(25);
        sectionsTable.getColumnModel().getColumn(0).setPreferredWidth(80);  // Section ID
        sectionsTable.getColumnModel().getColumn(1).setPreferredWidth(100); // Course Code
        sectionsTable.getColumnModel().getColumn(2).setPreferredWidth(200); // Course Title
        sectionsTable.getColumnModel().getColumn(3).setPreferredWidth(150); // Instructor
        
        JScrollPane sectionsScroll = new JScrollPane(sectionsTable);
        sectionsScroll.setPreferredSize(new Dimension(900, 300));
        panel.add(sectionsScroll, BorderLayout.CENTER);
        
        JPanel controlPanel = new JPanel(new FlowLayout());
        JButton refreshBtn = new JButton("Refresh Sections");
        
        refreshBtn.addActionListener(e -> refreshSectionsTable());
        
        controlPanel.add(refreshBtn);
        panel.add(controlPanel, BorderLayout.SOUTH);
        
        refreshSectionsTable();
        
        return panel;
    }

    private void refreshSectionsTable() {
        if (sectionsTable == null) return;
        
        DefaultTableModel model = (DefaultTableModel) sectionsTable.getModel();
        model.setRowCount(0);
        
        ApiResult<List<Section>> result = adminAPI.getAllSections();
        if (result.isSuccess() && result.getData() != null) {
            for (Section section : result.getData()) {
                String courseTitle = "";
                ApiResult<List<Course>> coursesResult = adminAPI.getAllCourses();
                if (coursesResult.isSuccess()) {
                    Course course = coursesResult.getData().stream()
                        .filter(c -> c.getCode().equals(section.getCourseCode()))
                        .findFirst()
                        .orElse(null);
                    if (course != null) {
                        courseTitle = course.getTitle();
                    }
                }
                
                String instructorName = "Not Assigned";
                if (section.getInstructorId() != null) {
                    ApiResult<User> userResult = adminAPI.getUserById(section.getInstructorId());
                    if (userResult.isSuccess() && userResult.getData() != null) {
                        instructorName = userResult.getData().getUsername();
                        
                        ApiResult<List<Instructor>> instructorsResult = adminAPI.getAllInstructors();
                        if (instructorsResult.isSuccess()) {
                            Instructor instructor = instructorsResult.getData().stream()
                                .filter(inst -> inst.getUserId() == section.getInstructorId())
                                .findFirst()
                                .orElse(null);
                            if (instructor != null) {
                                instructorName += " (" + instructor.getDepartment() + ")";
                            }
                        }
                    } else {
                        instructorName = "Instructor " + section.getInstructorId();
                    }
                }
                
                int availableSeats = section.getCapacity() - section.getEnrolledCount();
                
                model.addRow(new Object[]{
                    section.getSectionId(),
                    section.getCourseCode(),
                    courseTitle,
                    instructorName,
                    section.getDay().getDisplayName(),
                    section.getTime(),
                    section.getRoom(),
                    section.getCapacity(),
                    section.getEnrolledCount(),
                    availableSeats,
                    section.getSemester().getDisplayName(),
                    section.getYear()
                });
            }
            
            showMessage("Loaded " + model.getRowCount() + " sections");
        } else {
            showError("Failed to load sections: " + (result != null ? result.getMessage() : "Unknown error"));
        }
    }
    // System Settings Panel
    private JPanel createSystemSettingsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("System Settings", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel maintenancePanel = createMaintenancePanel();
        panel.add(maintenancePanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createMaintenancePanel() {
        JPanel panel = new JPanel(new FlowLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Maintenance Mode"));
        
        JLabel statusLabel = new JLabel("Checking status...");
        JButton enableBtn = new JButton("Enable Maintenance Mode");
        JButton disableBtn = new JButton("Disable Maintenance Mode");
        
        checkMaintenanceStatus(statusLabel);
        
        enableBtn.addActionListener(e -> {
            ApiResult<Boolean> result = adminAPI.setMaintenanceMode(true);
            if (result.isSuccess()) {
                showMessage("Maintenance mode enabled successfully!");
                checkMaintenanceStatus(statusLabel);
            } else {
                showError(result.getMessage());
            }
        });
        
        disableBtn.addActionListener(e -> {
            ApiResult<Boolean> result = adminAPI.setMaintenanceMode(false);
            if (result.isSuccess()) {
                showMessage("Maintenance mode disabled successfully!");
                checkMaintenanceStatus(statusLabel);
            } else {
                showError(result.getMessage());
            }
        });
        
        panel.add(statusLabel);
        panel.add(enableBtn);
        panel.add(disableBtn);
        
        return panel;
    }
    
    private void checkMaintenanceStatus(JLabel statusLabel) {
        ApiResult<Boolean> result = adminAPI.isMaintenanceMode();
        if (result.isSuccess()) {
            boolean maintenanceMode = Boolean.TRUE.equals(result.getData());
            if (maintenanceMode) {
                statusLabel.setText("Status: MAINTENANCE MODE ENABLED");
                statusLabel.setForeground(Color.RED);
            } else {
                statusLabel.setText("Status: System Normal");
                statusLabel.setForeground(Color.GREEN);
            }
        } else {
            statusLabel.setText("Status: Unknown");
            statusLabel.setForeground(Color.ORANGE);
        }
    }
}