package edu.univ.erp.util;

import java.io.IOException;
import java.util.logging.*;

public class Logger {
    private static final java.util.logging.Logger logger = 
        java.util.logging.Logger.getLogger("UniversityERP");
    private static final String LOG_FILE = "university_erp.log";
    
    static {
        setup();
    }
    
    public static void setup() {
        try {
            logger.setUseParentHandlers(false);
            
            FileHandler fileHandler = new FileHandler(LOG_FILE, true);
            fileHandler.setFormatter(new SimpleFormatter() {
                private static final String format = "[%1$tF %1$tT] [%2$-7s] %3$s %n";
                
                @Override
                public synchronized String format(LogRecord lr) {
                    return String.format(format,
                        new java.util.Date(lr.getMillis()),
                        lr.getLevel().getLocalizedName(),
                        lr.getMessage()
                    );
                }
            });
            
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new SimpleFormatter() {
                private static final String format = "[%1$tT] [%2$-7s] %3$s %n";
                
                @Override
                public synchronized String format(LogRecord lr) {
                    return String.format(format,
                        new java.util.Date(lr.getMillis()),
                        lr.getLevel().getLocalizedName(),
                        lr.getMessage()
                    );
                }
            });
            
            logger.addHandler(fileHandler);
            logger.addHandler(consoleHandler);
            
            logger.setLevel(Level.INFO);
            fileHandler.setLevel(Level.ALL);
            consoleHandler.setLevel(Level.INFO);
            
            info("University ERP System Logger initialized");
            
        } catch (IOException e) {
            System.err.println("Failed to setup logger: " + e.getMessage());
        }
    }
    
    public static void info(String message) {
        logger.info(message);
    }
    
    public static void warning(String message) {
        logger.warning(message);
    }
    
    public static void severe(String message) {
        logger.severe(message);
    }
    
    public static void config(String message) {
        logger.config(message);
    }
    
    public static void fine(String message) {
        logger.fine(message);
    }
    
    public static void debug(String methodName, String... params) {
        if (logger.isLoggable(Level.FINE)) {
            StringBuilder sb = new StringBuilder(methodName);
            if (params.length > 0) {
                sb.append(" - ");
                for (int i = 0; i < params.length; i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(params[i]);
                }
            }
            fine(sb.toString());
        }
    }
}