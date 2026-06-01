package edu.univ.erp.ui.common;

import javax.swing.*;

public class MessageHelper {
    
    public static void showSuccessDialog(JComponent parent, String message) {
        JOptionPane.showMessageDialog(
            parent, 
            message, 
            "Success", 
            JOptionPane.INFORMATION_MESSAGE
        );
    }
    
    public static void showErrorDialog(JComponent parent, String message) {
        JOptionPane.showMessageDialog(
            parent, 
            message, 
            "Error", 
            JOptionPane.ERROR_MESSAGE
        );
    }
    
    public static void showWarningDialog(JComponent parent, String message) {
        JOptionPane.showMessageDialog(
            parent, 
            message, 
            "Warning", 
            JOptionPane.WARNING_MESSAGE
        );
    }
    
    public static boolean showConfirmDialog(JComponent parent, String message) {
        int result = JOptionPane.showConfirmDialog(
            parent,
            message,
            "Confirm",
            JOptionPane.YES_NO_OPTION
        );
        return result == JOptionPane.YES_OPTION;
    }
    
    public static void showInfoMessage(JComponent parent, String title, String message) {
        JOptionPane.showMessageDialog(
            parent,
            message,
            title,
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}