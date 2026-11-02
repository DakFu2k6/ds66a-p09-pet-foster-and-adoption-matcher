CREATE TABLE IF NOT EXISTS pets (
    pet_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    species VARCHAR(20) NOT NULL CHECK (species IN ('DOG', 'CAT')),
    breed VARCHAR(100),
    age_months INTEGER NOT NULL CHECK (age_months >= 0),
    health_status VARCHAR(30) NOT NULL DEFAULT 'GOOD',
    temperament VARCHAR(30) NOT NULL DEFAULT 'FRIENDLY',
    special_care_required BOOLEAN NOT NULL DEFAULT FALSE,
    housing_requirement VARCHAR(30) NOT NULL DEFAULT 'ANY',
    status VARCHAR(30) NOT NULL DEFAULT 'AVAILABLE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS care_profiles (
    care_profile_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pet_id BIGINT NOT NULL UNIQUE REFERENCES pets (pet_id) ON DELETE CASCADE,
    daily_exercise_minutes INTEGER NOT NULL DEFAULT 30 CHECK (daily_exercise_minutes >= 0),
    feeding_notes TEXT,
    medical_notes TEXT
);

CREATE TABLE IF NOT EXISTS foster_families (
    family_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    family_name VARCHAR(120) NOT NULL,
    contact_name VARCHAR(120) NOT NULL,
    phone VARCHAR(30) NOT NULL,
    housing_type VARCHAR(30) NOT NULL,
    experience_level VARCHAR(30) NOT NULL,
    capacity INTEGER NOT NULL CHECK (capacity > 0),
    active_pet_count INTEGER NOT NULL DEFAULT 0 CHECK (active_pet_count >= 0),
    has_yard BOOLEAN NOT NULL DEFAULT FALSE,
    has_children BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CHECK (active_pet_count <= capacity)
);

CREATE TABLE IF NOT EXISTS applications (
    application_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pet_id BIGINT NOT NULL REFERENCES pets (pet_id),
    family_id BIGINT NOT NULL REFERENCES foster_families (family_id),
    application_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    notes TEXT,
    CHECK (
        status IN (
            'PENDING',
            'APPROVED',
            'REJECTED',
            'CANCELLED',
            'PLACED'
        )
    )
);

CREATE TABLE IF NOT EXISTS matching_results (
    matching_result_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    application_id BIGINT NOT NULL REFERENCES applications (application_id) ON DELETE CASCADE,
    logic_operator VARCHAR(10) NOT NULL CHECK (
        logic_operator IN ('AND', 'OR')
    ),
    overall_pass BOOLEAN NOT NULL,
    summary TEXT,
    evaluated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS matching_criteria_results (
    criterion_result_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    matching_result_id BIGINT NOT NULL REFERENCES matching_results (matching_result_id) ON DELETE CASCADE,
    criterion_name VARCHAR(80) NOT NULL,
    passed BOOLEAN NOT NULL,
    explanation TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS placements (
    placement_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    application_id BIGINT NOT NULL UNIQUE REFERENCES applications (application_id),
    pet_id BIGINT NOT NULL UNIQUE REFERENCES pets (pet_id),
    family_id BIGINT NOT NULL REFERENCES foster_families (family_id),
    placement_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (
        status IN (
            'ACTIVE',
            'COMPLETED',
            'CANCELLED'
        )
    )
);

CREATE INDEX IF NOT EXISTS idx_applications_pet ON applications (pet_id);

CREATE INDEX IF NOT EXISTS idx_applications_family ON applications (family_id);

CREATE INDEX IF NOT EXISTS idx_pets_status ON pets (status);