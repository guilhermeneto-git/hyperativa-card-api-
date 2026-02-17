package com.hyperativa.card.config;

import com.hyperativa.card.model.User;
import com.hyperativa.card.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

@Configuration
public class DataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    public CommandLineRunner initData(UserRepository userRepository,
                                      PasswordEncoder passwordEncoder,
                                      DataSource dataSource,
                                      Environment environment) {
        return args -> {
            // Skip SQL script execution during tests (H2 uses Hibernate DDL)
            boolean isTestEnvironment = isH2Database(dataSource);

            if (!isTestEnvironment) {

                // Check if tables exist (production/dev with MySQL)
                boolean tablesExist = checkIfTablesExist(dataSource);

                if (!tablesExist) {
                    log.info("=== Database tables not found. Executing create_db.sql ===");
                    executeSqlScript(dataSource);
                    log.info("=== Database and tables created successfully ===");
                } else {
                    log.info("=== Database tables already exist. Cleaning data for fresh start ===");
                    clearTablesData(dataSource);
                }
            } else {
                log.info("=== Test environment detected (H2). Skipping SQL script execution ===");
            }

            // Check if users already exist
            if (userRepository.count() == 0) {
                log.info("=== Creating default users ===");

                // Admin user
                User admin = new User();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setEmail("admin@hyperativa.com");
                admin.setRole("ADMIN");
                admin.setEnabled(true);
                userRepository.save(admin);
                log.info("✓ User 'admin' created successfully");

                // Regular user
                User user = new User();
                user.setUsername("user");
                user.setPassword(passwordEncoder.encode("user123"));
                user.setEmail("user@hyperativa.com");
                user.setRole("USER");
                user.setEnabled(true);
                userRepository.save(user);
                log.info("✓ User 'user' created successfully");

                log.info("=== {} users created ===", userRepository.count());
            } else {
                log.info("Users already exist in database. Total: {}", userRepository.count());
            }
        };
    }

    private boolean isH2Database(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            String url = connection.getMetaData().getURL();
            boolean isH2 = url != null && url.contains("h2");
            if (isH2) {
                log.debug("H2 database detected: {}", url);
            }
            return isH2;
        } catch (Exception e) {
            log.warn("Could not determine database type: {}", e.getMessage());
            return false;
        }
    }

    private boolean checkIfTablesExist(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();

            // Check if 'users' table exists
            try (ResultSet rs = metaData.getTables(null, null, "users", new String[]{"TABLE"})) {
                if (rs.next()) {
                    log.debug("Table 'users' found");
                    return true;
                }
            }

            // Check if 'cards' table exists
            try (ResultSet rs = metaData.getTables(null, null, "cards", new String[]{"TABLE"})) {
                if (rs.next()) {
                    log.debug("Table 'cards' found");
                    return true;
                }
            }

            log.debug("Tables not found in database");
            return false;
        } catch (Exception e) {
            log.warn("Could not check if tables exist: {}", e.getMessage());
            return false;
        }
    }

    private void executeSqlScript(DataSource dataSource) {
        try {
            ClassPathResource resource = new ClassPathResource("db/create_db.sql");

            if (!resource.exists()) {
                log.error("SQL script not found at: db/create_db.sql");
                log.info("Please ensure the file exists in src/main/resources/db/create_db.sql");
                return;
            }

            ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
            populator.addScript(resource);
            populator.setContinueOnError(false);
            populator.setSeparator(";");

            populator.execute(dataSource);

            log.info("SQL script executed successfully");
        } catch (Exception e) {
            log.error("Error executing SQL script: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to execute database initialization script", e);
        }
    }

    /**
     * Clears all data from cards and users tables
     * Resets auto_increment IDs to 1
     * Useful for testing and development environments
     */
    private void clearTablesData(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);

            try (var statement = connection.createStatement()) {
                // Delete cards first (no foreign keys to worry about)
                int cardsDeleted = statement.executeUpdate("DELETE FROM cards");
                log.info("✓ Deleted {} records from 'cards' table", cardsDeleted);

                // Reset auto_increment for cards table
                statement.executeUpdate("ALTER TABLE cards AUTO_INCREMENT = 1");
                log.info("✓ Reset auto_increment for 'cards' table to 1");

                // Delete users
                int usersDeleted = statement.executeUpdate("DELETE FROM users");
                log.info("✓ Deleted {} records from 'users' table", usersDeleted);

                // Reset auto_increment for users table
                statement.executeUpdate("ALTER TABLE users AUTO_INCREMENT = 1");
                log.info("✓ Reset auto_increment for 'users' table to 1");

                connection.commit();
                log.info("=== Tables cleaned and IDs reset successfully ===");
            } catch (Exception e) {
                connection.rollback();
                throw e;
            }
        } catch (Exception e) {
            log.error("Error clearing tables data: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to clear tables data", e);
        }
    }
}

