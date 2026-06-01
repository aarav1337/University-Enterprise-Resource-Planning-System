package edu.univ.erp.util;

import javax.swing.*;
import java.awt.*;

public class MessageHelper {
    
    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Information", JOptionPane.INFORMATION_MESSAGE);
        Logger.info("UI Info: " + message);
    }
    
    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
        Logger.severe("UI Error: " + message);
    }
    
    public static void showWarning(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Warning", JOptionPane.WARNING_MESSAGE);
        Logger.warning("UI Warning: " + message);
    }
    
    public static void showSuccess(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Success", JOptionPane.INFORMATION_MESSAGE);
        Logger.info("UI Success: " + message);
    }
    
    public static boolean showConfirm(Component parent, String message) {
        int result = JOptionPane.showConfirmDialog(parent, message, "Confirm", 
                                                  JOptionPane.YES_NO_OPTION);
        Logger.info("UI Confirm: " + message + " - User selected: " + (result == JOptionPane.YES_OPTION ? "Yes" : "No"));
        return result == JOptionPane.YES_OPTION;
    }
    
    public static boolean showConfirm(Component parent, String message, String title) {
        int result = JOptionPane.showConfirmDialog(parent, message, title, 
                                                  JOptionPane.YES_NO_OPTION);
        Logger.info("UI Confirm [" + title + "]: " + message + " - User selected: " + (result == JOptionPane.YES_OPTION ? "Yes" : "No"));
        return result == JOptionPane.YES_OPTION;
    }
    
    public static String showInput(Component parent, String message) {
        String result = JOptionPane.showInputDialog(parent, message);
        Logger.info("UI Input: " + message + " - User entered: " + (result != null ? result : "null"));
        return result;
    }
    
    public static String showInput(Component parent, String message, String defaultValue) {
        String result = (String) JOptionPane.showInputDialog(parent, message, 
                                                           "Input", JOptionPane.QUESTION_MESSAGE, 
                                                           null, null, defaultValue);
        Logger.info("UI Input: " + message + " - User entered: " + (result != null ? result : "null"));
        return result;
    }
    
    public static int showOptionDialog(Component parent, String message, String title, 
                                     String[] options, String defaultOption) {
        int result = JOptionPane.showOptionDialog(parent, message, title, 
                                                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, 
                                                null, options, defaultOption);
        Logger.info("UI Option [" + title + "]: " + message + " - User selected option: " + result);
        return result;
    }
}