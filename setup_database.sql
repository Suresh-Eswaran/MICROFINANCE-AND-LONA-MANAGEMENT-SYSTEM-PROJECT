-- ============================================================
-- Microfinance Loan Management System — Database Setup Script
-- Run this in MySQL Workbench or MySQL CLI:
--   mysql -u root -p < setup_database.sql
-- ============================================================

-- 1. Create the database
CREATE DATABASE IF NOT EXISTS microfinance_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE microfinance_db;

-- ============================================================
-- CORE TABLES (5 entities per spec)
-- ============================================================

-- 2. Users table
CREATE TABLE IF NOT EXISTS users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    full_name   VARCHAR(100)    NOT NULL,
    email       VARCHAR(150)    NOT NULL UNIQUE,
    password    VARCHAR(255)    NOT NULL,
    role        VARCHAR(30)     NOT NULL
                    COMMENT 'ADMIN | BRANCH_MANAGER | CREDIT_OFFICER | LOAN_OFFICER | COLLECTIONS_AGENT | CLIENT',
    branch      VARCHAR(100),
    status      VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE'
                    COMMENT 'ACTIVE | INACTIVE | SUSPENDED'
);

-- 3. Clients table
CREATE TABLE IF NOT EXISTS clients (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    name         VARCHAR(100)    NOT NULL
                     COMMENT 'Alphabetic only, 2-100 chars',
    phone_number VARCHAR(10)     NOT NULL
                     COMMENT 'Exactly 10 digits',
    aadhaar_hash VARCHAR(64)
                     COMMENT 'SHA-256 hash of raw Aadhaar — never plain text',
    pan_number   VARCHAR(10)     COMMENT 'Stubbed — no live PAN verification',
    cibil_score  INT             CHECK (cibil_score BETWEEN 300 AND 900)
                     COMMENT 'Nullable — no live bureau call',
    group_id     BIGINT          COMMENT 'FK stub — Groups table not implemented',
    kyc_status   VARCHAR(20)     NOT NULL DEFAULT 'PENDING'
                     COMMENT 'PENDING | VERIFIED | REJECTED',
    created_date DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 4. Loan Applications table
CREATE TABLE IF NOT EXISTS loan_applications (
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    client_id            BIGINT          NOT NULL,
    product_id           BIGINT          COMMENT 'Nullable FK — LoanProducts table not implemented',
    amount_requested     DECIMAL(12,2)   NOT NULL CHECK (amount_requested > 0),
    purpose              VARCHAR(200),
    status               VARCHAR(25)     NOT NULL DEFAULT 'SUBMITTED'
                             COMMENT 'SUBMITTED | UNDER_REVIEW | APPROVED | REJECTED | DISBURSED | CLOSED | NPA',
    officer_id           BIGINT          COMMENT 'FK to users — must be set before approval',
    applied_date         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tenure_months        INT             COMMENT 'Used for EMI schedule generation',
    annual_interest_rate DECIMAL(5,2)    COMMENT 'e.g. 12.50 for 12.5% p.a.',

    CONSTRAINT fk_loan_client  FOREIGN KEY (client_id)  REFERENCES clients(id),
    CONSTRAINT fk_loan_officer FOREIGN KEY (officer_id) REFERENCES users(id)
);

-- 5. EMI Schedule table
CREATE TABLE IF NOT EXISTS emi_schedule (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_id        BIGINT          NOT NULL,
    installment_no INT             NOT NULL,
    due_date       DATE            NOT NULL,
    principal      DECIMAL(10,2),
    interest       DECIMAL(10,2),
    emi_amount     DECIMAL(10,2),
    status         VARCHAR(20)     NOT NULL DEFAULT 'PENDING'
                       COMMENT 'PENDING | PAID | PARTIAL | OVERDUE | WAIVED',
    paid_date      DATETIME,

    CONSTRAINT fk_emi_loan FOREIGN KEY (loan_id) REFERENCES loan_applications(id)
);

-- 6. Collections table
CREATE TABLE IF NOT EXISTS collections (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    emi_id           BIGINT          NOT NULL,
    collected_by     BIGINT          NOT NULL COMMENT 'FK to users (collections agent)',
    amount_collected DECIMAL(10,2)   NOT NULL CHECK (amount_collected > 0),
    collection_date  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gps_lat          DECIMAL(9,6)    COMMENT 'Optional — field collection GPS',
    gps_lng          DECIMAL(9,6)    COMMENT 'Optional — field collection GPS',
    receipt_number   VARCHAR(30)     NOT NULL UNIQUE,

    CONSTRAINT fk_collection_emi   FOREIGN KEY (emi_id)       REFERENCES emi_schedule(id),
    CONSTRAINT fk_collection_agent FOREIGN KEY (collected_by) REFERENCES users(id)
);

-- ============================================================
-- SUPPORTING TABLES (kept for JPA entity compatibility)
-- ============================================================

CREATE TABLE IF NOT EXISTS audit_logs (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    timestamp  DATETIME        NOT NULL,
    action     VARCHAR(100)    NOT NULL,
    user_id    BIGINT,
    email      VARCHAR(150)    NOT NULL,
    ip_address VARCHAR(50)     NOT NULL,
    details    VARCHAR(2000)
);

CREATE TABLE IF NOT EXISTS microfinance_directory (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_or_user_id VARCHAR(50)  NOT NULL UNIQUE,
    email               VARCHAR(150) NOT NULL,
    full_name           VARCHAR(100) NOT NULL,
    role                VARCHAR(30)  NOT NULL
);

-- ============================================================
-- VERIFY
-- ============================================================
SHOW TABLES;

SELECT 'Database setup complete! All 6 core tables created.' AS status;
