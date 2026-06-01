package edu.univ.erp.ui.common;

import edu.univ.erp.api.MaintenanceAPI;
import edu.univ.erp.auth.SessionManager;
import edu.univ.erp.domain.UserRole;
import edu.univ.erp.ui.LoginFrame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public abstract class MainFrame extends JFrame {
    protected SessionManager sessionManager;
    protected MaintenanceAPI maintenanceAPI;
    
    protected JPanel mainPanel;
    protected JLabel userInfoLabel;
    protected JLabel maintenanceLabel;
    protected JButton logoutButton;
    
    private String frameTitle;
    
    public MainFrame(String title) {
        this.frameTitle = title;
        this.sessionManager = SessionManager.getInstance();
        this.maintenanceAPI = new MaintenanceAPI();
        initializeFrame();
        initializeUI();
    }
    
    private void initializeFrame() {
        setTitle("University ERP - " + frameTitle);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 700);
        setLocationRelativeTo(null);
    }
    
    protected void initializeUI() {
        mainPanel = new JPanel(new BorderLayout());
        
        JPanel headerPanel = createHeaderPanel();
        mainPanel.add(headerPanel, BorderLayout.NORTH);
        
        JPanel contentPanel = createContentPanel();
        mainPanel.add(contentPanel, BorderLayout.CENTER);
        
        JPanel statusPanel = createStatusPanel();
        mainPanel.add(statusPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
    }
    
    private JPanel createHeaderPanel() {
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(240, 240, 240));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        
        userInfoLabel = new JLabel();
        updateUserInfo();
        headerPanel.add(userInfoLabel, BorderLayout.WEST);
        
        JLabel titleLabel = new JLabel(frameTitle, SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        logoutButton = new JButton("Logout");
        logoutButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });
        headerPanel.add(logoutButton, BorderLayout.EAST);
        
        return headerPanel;
    }
    
    protected JPanel createStatusPanel() {
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusPanel.setBorder(BorderFactory.createEtchedBorder());
        
        maintenanceLabel = new JLabel();
        updateMaintenanceStatus();
        
        statusPanel.add(maintenanceLabel);
        
        Timer maintenanceTimer = new Timer(5000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateMaintenanceStatus();
            }
        });
        maintenanceTimer.start();
        
        return statusPanel;
    }
    
    protected void updateUserInfo() {
        if (sessionManager.isLoggedIn()) {
            String userInfo = String.format("Welcome, %s (%s)", 
                sessionManager.getCurrentUsername(),
                sessionManager.getCurrentUserRole().getDisplayName());
            userInfoLabel.setText(userInfo);
        }
    }
    
    protected void updateMaintenanceStatus() {
        MaintenanceAPI.ApiResult<Boolean> result = maintenanceAPI.isMaintenanceMode();
        if (result.isSuccess() && result.getData()) {
            maintenanceLabel.setText("MAINTENANCE MODE - READ ONLY");
            maintenanceLabel.setForeground(Color.RED);
            maintenanceLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        } else {
            maintenanceLabel.setText("System Normal");
            maintenanceLabel.setForeground(Color.GREEN);
            maintenanceLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        }
    }
    
    protected void logout() {
        int confirm = JOptionPane.showConfirmDialog(
            this,
            "Are you sure you want to logout?",
            "Confirm Logout",
            JOptionPane.YES_NO_OPTION
        );
        
        if (confirm == JOptionPane.YES_OPTION) {
            sessionManager.logout();
            new LoginFrame().setVisible(true);
            dispose();
        }
    }
    
    protected void showMessage(String message) {
        JOptionPane.showMessageDialog(this, message);
    }
    
    protected void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE);
    }
    
    protected boolean confirmAction(String message) {
        int result = JOptionPane.showConfirmDialog(
            this,
            message,
            "Confirm Action",
            JOptionPane.YES_NO_OPTION
        );
        return result == JOptionPane.YES_OPTION;
    }
    
    protected abstract JPanel createContentPanel();
    
    public String getFrameTitle() {
        return frameTitle;
    }
}