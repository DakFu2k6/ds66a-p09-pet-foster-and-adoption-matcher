USE pet_matcher_db;

CREATE TABLE IF NOT EXISTS pets (
    pet_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    species VARCHAR(20) NOT NULL,
    breed VARCHAR(100),
    age_months INT NOT NULL,
    health_status VARCHAR(30) NOT NULL DEFAULT 'GOOD',
    temperament VARCHAR(30) NOT NULL DEFAULT 'FRIENDLY',
    special_care_required BOOLEAN NOT NULL DEFAULT FALSE,
    housing_requirement VARCHAR(30) NOT NULL DEFAULT 'ANY',
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_pets_species CHECK (species IN ('DOG', 'CAT')),
    CONSTRAINT chk_pets_age CHECK (age_months >= 0),
    INDEX idx_pets_status (status)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS care_profiles (
    care_profile_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    pet_id BIGINT NOT NULL UNIQUE,
    daily_exercise_minutes INT NOT NULL DEFAULT 30,
    feeding_notes TEXT,
    medical_notes TEXT,
    CONSTRAINT fk_care_pet FOREIGN KEY (pet_id) REFERENCES pets (pet_id) ON DELETE CASCADE,
    CONSTRAINT chk_care_exercise CHECK (daily_exercise_minutes >= 0)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS foster_families (
    family_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    family_name VARCHAR(120) NOT NULL,
    contact_name VARCHAR(120) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    housing_type VARCHAR(30) NOT NULL,
    experience_level VARCHAR(30) NOT NULL,
    capacity INT NOT NULL,
    active_pet_count INT NOT NULL DEFAULT 0,
    has_yard BOOLEAN NOT NULL DEFAULT FALSE,
    has_children BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_family_capacity CHECK (capacity > 0),
    CONSTRAINT chk_family_active_count CHECK (active_pet_count >= 0),
    CONSTRAINT chk_family_not_over CHECK (active_pet_count <= capacity)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS applications (
    application_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    pet_id BIGINT NOT NULL,
    family_id BIGINT NOT NULL,
    application_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    CONSTRAINT fk_application_pet FOREIGN KEY (pet_id) REFERENCES pets (pet_id),
    CONSTRAINT fk_application_family FOREIGN KEY (family_id) REFERENCES foster_families (family_id),
    CONSTRAINT chk_application_status CHECK (
        status IN (
            'PENDING',
            'APPROVED',
            'REJECTED',
            'CANCELLED',
            'PLACED'
        )
    ),
    INDEX idx_applications_pet (pet_id),
    INDEX idx_applications_family (family_id)
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS matching_results (
    matching_result_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL,
    logic_operator VARCHAR(10) NOT NULL,
    overall_pass BOOLEAN NOT NULL,
    summary TEXT,
    evaluated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_mr_application FOREIGN KEY (application_id) REFERENCES applications (application_id) ON DELETE CASCADE,
    CONSTRAINT chk_mr_operator CHECK (
        logic_operator IN ('AND', 'OR')
    )
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS matching_criteria_results (
    criterion_result_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    matching_result_id BIGINT NOT NULL,
    criterion_name VARCHAR(80) NOT NULL,
    passed BOOLEAN NOT NULL,
    explanation TEXT NOT NULL,
    CONSTRAINT fk_mcr_result FOREIGN KEY (matching_result_id) REFERENCES matching_results (matching_result_id) ON DELETE CASCADE
) ENGINE = InnoDB;

CREATE TABLE IF NOT EXISTS placements (
    placement_id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    application_id BIGINT NOT NULL UNIQUE,
    pet_id BIGINT NOT NULL UNIQUE,
    family_id BIGINT NOT NULL,
    placement_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT fk_placement_application FOREIGN KEY (application_id) REFERENCES applications (application_id),
    CONSTRAINT fk_placement_pet FOREIGN KEY (pet_id) REFERENCES pets (pet_id),
    CONSTRAINT fk_placement_family FOREIGN KEY (family_id) REFERENCES foster_families (family_id),
    CONSTRAINT chk_placement_status CHECK (
        status IN (
            'ACTIVE',
            'COMPLETED',
            'CANCELLED'
        )
    )
) ENGINE = InnoDB;