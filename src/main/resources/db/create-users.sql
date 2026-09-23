-- Run once against the application's online_shop database.
-- Re-running preserves existing accounts and does not reset the admin password.
CREATE TABLE IF NOT EXISTS users (
    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(254) NOT NULL UNIQUE,
    password VARCHAR(64) NOT NULL,
    salt VARCHAR(64) NOT NULL,
    isLock BIT(1) NOT NULL DEFAULT 0,
    isVerify BIT(1) NOT NULL DEFAULT 0,
    created_date DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_date DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
);

SET @admin_salt = LOWER(HEX(RANDOM_BYTES(32)));
INSERT INTO users (username, email, password, salt, isLock, isVerify, created_date, updated_date)
SELECT 'admin', 'admin@localhost', LOWER(SHA2(CONCAT(@admin_salt, '1234'), 256)), @admin_salt, 0, 0, CURRENT_TIMESTAMP(6), CURRENT_TIMESTAMP(6)
WHERE NOT EXISTS (SELECT 1 FROM users WHERE LOWER(username) = 'admin');
