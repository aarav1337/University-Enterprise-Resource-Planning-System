package edu.univ.erp.ui;

import com.formdev.flatlaf.FlatLightLaf;
import net.miginfocom.swing.MigLayout;
import edu.univ.erp.api.AuthAPI;
import edu.univ.erp.domain.UserRole;
import edu.univ.erp.ui.admin.AdminDashboard;
import edu.univ.erp.ui.instructor.InstructorDashboard;
import edu.univ.erp.ui.student.StudentDashboard;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

public class LoginFrame extends JFrame {
    private static final Logger logger = Logger.getLogger(LoginFrame.class.getName());
    
    private final AuthAPI authAPI;
    
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;
    private JComboBox<String> roleComboBox;
    
    public LoginFrame() {
        this.authAPI = new AuthAPI();
        initializeUI();
    }
    
    private void initializeUI() {
        setTitle("University ERP System - Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);
        
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (UnsupportedLookAndFeelException e) {
            logger.warning("FlatLaf not available, using system look and feel");
        }
        
        JPanel mainPanel = new JPanel(new MigLayout(
            "insets 30, fillx, wrap 2", 
            "[right][grow, 250]",
            "[]10[]10[]10[]10[]"
        ));
        
        JLabel titleLabel = new JLabel("UNIVERSITY ERP SYSTEM");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLabel.setForeground(new Color(0, 70, 130));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel, "span 2, growx, gapbottom 30");
        
        mainPanel.add(new JLabel("Login As:"));
        roleComboBox = new JComboBox<>(new String[]{"Student", "Instructor", "Admin"});
        mainPanel.add(roleComboBox, "growx");
        
        mainPanel.add(new JLabel("Username:"));
        usernameField = new JTextField();
        mainPanel.add(usernameField, "growx");
        
        mainPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField();
        mainPanel.add(passwordField, "growx");
        
        loginButton = new JButton("Login");
        loginButton.setBackground(new Color(0, 120, 215));
        loginButton.setForeground(Color.WHITE);
        loginButton.setFocusPainted(false);
        mainPanel.add(loginButton, "span 2, growx, gaptop 20, h 35!");
        
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(statusLabel, "span 2, growx");
        
        JLabel demoLabel = new JLabel("<html><center>Demo Credentials:<br>"
            + "Student: stu1/stu123 | Instructor: inst1/inst123 | Admin: admin1/admin123</center></html>");
        demoLabel.setForeground(Color.GRAY);
        demoLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
        demoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(demoLabel, "span 2, growx, gaptop 20");
        
        setupEventListeners();
        
        add(mainPanel);
        pack();
        setLocationRelativeTo(null); 
        
        getRootPane().setDefaultButton(loginButton);
    }
    
    private void setupEventListeners() {
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
        
        roleComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                autoFillCredentials();
            }
        });
        
        passwordField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                performLogin();
            }
        });
    }
    
    private void autoFillCredentials() {
        String role = (String) roleComboBox.getSelectedItem();
        switch (role) {
            case "Student":
                usernameField.setText("stu1");
                passwordField.setText("stu123");
                break;
            case "Instructor":
                usernameField.setText("inst1");
                passwordField.setText("inst123");
                break;
            case "Admin":
                usernameField.setText("admin1");
                passwordField.setText("admin123");
                break;
        }
    }
    
    private void performLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }
        
        loginButton.setText("Logging in...");
        loginButton.setEnabled(false);
        
        new Thread(() -> {
            try {
                Thread.sleep(500); 
                
                SwingUtilities.invokeLater(() -> {
                    AuthAPI.LoginResult result = authAPI.login(username, password);
                    
                    if (result.isSuccess()) {
                        UserRole role = result.getRole();
                        openRoleSpecificDashboard(role);
                        dispose(); 
                    } else {
                        showError(result.getMessage());
                        passwordField.setText("");
                        usernameField.requestFocus();
                    }
                    
                    loginButton.setText("Login");
                    loginButton.setEnabled(true);
                });
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }
    
    private void openRoleSpecificDashboard(UserRole role) {
        switch (role) {
            case STUDENT:
                new StudentDashboard().setVisible(true);
                break;
            case INSTRUCTOR:
                new InstructorDashboard().setVisible(true);
                break;
            case ADMIN:
                new AdminDashboard().setVisible(true);
                break;
            default:
                logger.warning("Unknown user role: " + role);
                showError("Unknown user role. Access denied.");
        }
    }
    
    private void showError(String message) {
        statusLabel.setText(message);
        Timer timer = new Timer(5000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                statusLabel.setText(" ");
            }
        });
        timer.setRepeats(false);
        timer.start();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}