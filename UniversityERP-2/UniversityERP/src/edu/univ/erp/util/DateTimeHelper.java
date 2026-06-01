package edu.univ.erp.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.Logger;

public class DateTimeHelper {
    private static final Logger logger = Logger.getLogger(DateTimeHelper.class.getName());
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    public static String getCurrentDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }
    
    public static String getCurrentDateTime() {
        return LocalDateTime.now().format(DATETIME_FORMATTER);
    }
    
    public static String getCurrentTime() {
        return LocalDateTime.now().format(TIME_FORMATTER);
    }
    
    public static boolean isBeforeDeadline(String deadline) {
        try {
            LocalDate deadlineDate = LocalDate.parse(deadline, DATE_FORMATTER);
            return LocalDate.now().isBefore(deadlineDate);
        } catch (Exception e) {
            logger.warning("Invalid deadline format: " + deadline);
            return false;
        }
    }
    
    public static boolean isAfterDeadline(String deadline) {
        return !isBeforeDeadline(deadline);
    }
    
    public static String formatDate(String dateString) {
        try {
            LocalDate date = LocalDate.parse(dateString, DATE_FORMATTER);
            return date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
        } catch (Exception e) {
            return dateString;
        }
    }
    
    public static boolean isValidDate(String dateString) {
        try {
            LocalDate.parse(dateString, DATE_FORMATTER);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    public static String addDays(String dateString, int days) {
        try {
            LocalDate date = LocalDate.parse(dateString, DATE_FORMATTER);
            return date.plusDays(days).format(DATE_FORMATTER);
        } catch (Exception e) {
            logger.warning("Error adding days to date: " + dateString);
            return dateString;
        }
    }
}