package com.hyperativa.card.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

/**
 * Ensures the MySQL database exists before Spring Boot tries to connect to it.
 * This initializer runs before any bean creation, including the DataSource.
 */
public class DatabaseInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        Environment env = applicationContext.getEnvironment();

        String url = env.getProperty("spring.datasource.url");
        String username = env.getProperty("spring.datasource.username");
        String password = env.getProperty("spring.datasource.password");

        // Only proceed if MySQL is configured
        if (url == null || !url.contains("mysql")) {
            log.debug("Not a MySQL database, skipping database creation");
            return;
        }

        try {
            // Extract database name
            String dbName = extractDatabaseName(url);
            if (dbName == null) {
                log.warn("Could not extract database name from URL: {}", url);
                return;
            }

            // Build connection URL without database name
            String baseUrl = url.substring(0, url.lastIndexOf('/'));
            int questionMarkIndex = baseUrl.indexOf('?');
            if (questionMarkIndex != -1) {
                baseUrl = baseUrl.substring(0, questionMarkIndex);
            }
            baseUrl += "?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";

            log.info("=== Ensuring MySQL database '{}' exists ===", dbName);

            // Connect and create database
            try (Connection conn = DriverManager.getConnection(baseUrl, username, password);
                 Statement stmt = conn.createStatement()) {

                String createDb = "CREATE DATABASE IF NOT EXISTS " + dbName +
                                 " CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci";
                stmt.executeUpdate(createDb);

                log.info("✓ Database '{}' is ready", dbName);
            }

        } catch (Exception e) {
            log.error("Failed to ensure database exists: {}", e.getMessage(), e);
            // Don't throw exception - let Spring Boot handle the connection error
        }
    }

    private String extractDatabaseName(String url) {
        try {
            // URL format: jdbc:mysql://localhost:3306/card_db?params
            int lastSlash = url.lastIndexOf('/');
            if (lastSlash == -1) {
                return null;
            }

            String afterSlash = url.substring(lastSlash + 1);
            int questionMark = afterSlash.indexOf('?');

            if (questionMark != -1) {
                return afterSlash.substring(0, questionMark);
            } else {
                return afterSlash;
            }
        } catch (Exception e) {
            log.error("Error extracting database name: {}", e.getMessage());
            return null;
        }
    }
}

