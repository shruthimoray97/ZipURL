-- ======================================================
-- ZipURL Database Initialization Script
-- MySQL Database Setup
-- ======================================================

-- 1. Create database if not exists
CREATE DATABASE IF NOT EXISTS zipurl_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE zipurl_db;

-- 2. Create URLs table schema
CREATE TABLE IF NOT EXISTS urls (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_url VARCHAR(2048) NOT NULL,
    short_code VARCHAR(32) NOT NULL UNIQUE,
    created_at DATETIME NOT NULL,
    click_count BIGINT NOT NULL DEFAULT 0,
    INDEX idx_short_code (short_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Optional sample data for testing
-- INSERT INTO urls (original_url, short_code, created_at, click_count) 
-- VALUES ('https://spring.io/projects/spring-boot', 'boot33', NOW(), 42);
