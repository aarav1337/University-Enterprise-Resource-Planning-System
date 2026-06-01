package edu.univ.erp.data;

import edu.univ.erp.domain.Settings;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

public class SettingsDAO {
    private static final Logger logger = Logger.getLogger(SettingsDAO.class.getName());
    
    public boolean setMaintenanceMode(boolean enabled) {
        String sql = """
            INSERT INTO university_erp.settings (setting_key, setting_value) 
            VALUES ('maintenance_mode', ?)
            ON DUPLICATE KEY UPDATE setting_value = ?
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            String value = enabled ? "true" : "false";
            pstmt.setString(1, value);
            pstmt.setString(2, value);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            logger.severe("Error setting maintenance mode: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public boolean isMaintenanceModeEnabled() {
        String sql = "SELECT setting_value FROM university_erp.settings WHERE setting_key = 'maintenance_mode'";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            if (rs.next()) {
                return Boolean.parseBoolean(rs.getString("setting_value"));
            }
            
        } catch (SQLException e) {
            logger.severe("Error checking maintenance mode: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false; // Default to false if not set
    }
    
    public Settings getSetting(String key) {
        String sql = "SELECT setting_key, setting_value FROM university_erp.settings WHERE setting_key = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, key);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Settings(
                    rs.getString("setting_key"),
                    rs.getString("setting_value")
                );
            }
            
        } catch (SQLException e) {
            logger.severe("Error getting setting: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    public boolean updateSetting(String key, String value) {
        String sql = """
            INSERT INTO university_erp.settings (setting_key, setting_value) 
            VALUES (?, ?)
            ON DUPLICATE KEY UPDATE setting_value = ?
            """;
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, key);
            pstmt.setString(2, value);
            pstmt.setString(3, value);
            
            int rowsAffected = pstmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            logger.severe("Error updating setting: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    public List<Settings> getAllSettings() {
        List<Settings> settings = new ArrayList<>();
        String sql = "SELECT setting_key, setting_value FROM university_erp.settings ORDER BY setting_key";
        
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                Settings setting = new Settings(
                    rs.getString("setting_key"),
                    rs.getString("setting_value")
                );
                settings.add(setting);
            }
            
        } catch (SQLException e) {
            logger.severe("Error getting all settings: " + e.getMessage());
            e.printStackTrace();
        }
        
        return settings;
    }
}