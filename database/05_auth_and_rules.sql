USE pet_matcher_db;

CREATE TABLE IF NOT EXISTS staff_users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    display_name VARCHAR(120) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    failed_attempts INT NOT NULL DEFAULT 0,
    locked_until DATETIME NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS match_criteria (
    code VARCHAR(40) PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    sort_order INT NOT NULL
) ENGINE = InnoDB;

INSERT IGNORE INTO
    match_criteria
VALUES (
        'HOUSING',
        'Required housing and yard conditions',
        10
    ),
    (
        'EXPERIENCE',
        'Species-specific care experience',
        20
    ),
    (
        'HOUSEHOLD',
        'Temperament and household children',
        30
    );