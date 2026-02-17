-- =============================================
-- Card API - Database Initialization Script
-- For Spring Boot ResourceDatabasePopulator
-- Database: MySQL 8.0+
-- =============================================

-- Drop tables if they exist (in correct order due to foreign keys)
DROP TABLE IF EXISTS cards;
DROP TABLE IF EXISTS users;

-- =============================================
-- Table: users
-- Description: Stores user authentication data
-- =============================================
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    role VARCHAR(20) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username),
    INDEX idx_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- Table: cards
-- Description: Stores card numbers
-- =============================================
CREATE TABLE IF NOT EXISTS cards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    card_number BIGINT NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_card_number (card_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================
-- Insert default users
-- Password for both users: admin123 and user123 (BCrypt encoded)
-- Note: These will be inserted by DataInitializer.java
-- Uncomment below if you want to insert via SQL:
-- =============================================
-- INSERT INTO users (username, password, email, role, enabled) VALUES
--     ('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8S8gPxkJv6eEqpSJYyLfq5HzgO3n2', 'admin@hyperativa.com', 'ADMIN', TRUE),
--     ('user', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J8S8gPxkJv6eEqpSJYyLfq5HzgO3n2', 'user@hyperativa.com', 'USER', TRUE)
-- ON DUPLICATE KEY UPDATE updated_at = CURRENT_TIMESTAMP;

