package edu.univ.erp.data;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

public class DatabaseConnection {
    private static final Logger logger = Logger.getLogger(DatabaseConnection.class.getName());
    
    private static DatabaseConnection instance;
    private static HikariDataSource dataSource;
  
    private static final String DB_URL = "jdbc:mysql://localhost:3306";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "root";
    
    public DatabaseConnection() {
        initializeDataSource();
    }
    
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }
    
    private void initializeDataSource() {
        try {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(DB_URL + "?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true");
            config.setUsername(DB_USER);
            config.setPassword(DB_PASSWORD);
            config.setDriverClassName("com.mysql.cj.jdbc.Driver");
            
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            config.setLeakDetectionThreshold(60000);
            
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            config.addDataSourceProperty("useLocalSessionState", "true");
            config.addDataSourceProperty("rewriteBatchedStatements", "true");
            config.addDataSourceProperty("cacheResultSetMetadata", "true");
            config.addDataSourceProperty("cacheServerConfiguration", "true");
            config.addDataSourceProperty("elideSetAutoCommits", "true");
            config.addDataSourceProperty("maintainTimeStats", "false");
            
            dataSource = new HikariDataSource(config);
            logger.info("HikariCP DataSource initialized successfully");
            
            try (Connection conn = dataSource.getConnection()) {
                logger.info("Database connection test successful");
            }
            
        } catch (Exception e) {
            logger.severe("Failed to initialize DataSource: " + e.getMessage());
            throw new RuntimeException("DataSource initialization failed", e);
        }
    }
    
    public static Connection getConnection() throws SQLException {
    	 if (dataSource == null) {
    	        getInstance(); 
    	    }
    	    return dataSource.getConnection();
    }
    
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("DataSource closed successfully");
        }
    }
    
    public static String getDatabaseURL(String databaseName) {
        return DB_URL + "/" + databaseName + "?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=UTC";
    }
}