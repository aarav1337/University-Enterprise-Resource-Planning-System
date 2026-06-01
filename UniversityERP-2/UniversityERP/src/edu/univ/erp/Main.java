package edu.univ.erp;

import com.formdev.flatlaf.FlatLightLaf;
import edu.univ.erp.data.DatabaseInitializer;
import edu.univ.erp.ui.LoginFrame;
import edu.univ.erp.util.Logger;

import java.awt.Font;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
       Logger.setup();
        Logger.info("Starting University ERP System");
        
        initializeDatabase();
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(new FlatLightLaf());
                setUIDefaults();
                
            } catch (UnsupportedLookAndFeelException e) {
                Logger.warning("FlatLaf not available, using system look and feel");
            }
            
            new LoginFrame().setVisible(true);
            Logger.info("Login frame displayed");
        });
    }
    
    private static void initializeDatabase() {
        try {
            DatabaseInitializer.initializeDatabases();
            Logger.info("Database initialization completed");
        } catch (Exception e) {
            Logger.severe("Database initialization failed: " + e.getMessage());
            JOptionPane.showMessageDialog(null, 
                "Database initialization failed. Please check database connection.\n" + e.getMessage(),
                "Startup Error",
                JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }
    
    private static void setUIDefaults() {
        UIManager.put("Button.arc", 10);
        UIManager.put("Component.arc", 10);
        UIManager.put("TextComponent.arc", 5);
        
        Font defaultFont = new Font("SansSerif", Font.PLAIN, 12);
        UIManager.put("Button.font", defaultFont);
        UIManager.put("Label.font", defaultFont);
        UIManager.put("TextField.font", defaultFont);
        UIManager.put("ComboBox.font", defaultFont);
        UIManager.put("Table.font", defaultFont);
        UIManager.put("TableHeader.font", defaultFont.deriveFont(Font.BOLD));
    }
}