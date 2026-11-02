INSERT INTO
    pets (
        name,
        species,
        breed,
        age_months,
        health_status,
        temperament,
        special_care_required,
        housing_requirement,
        status
    )
VALUES (
        'Milo',
        'DOG',
        'Golden Retriever',
        18,
        'GOOD',
        'FRIENDLY',
        FALSE,
        'HOUSE',
        'AVAILABLE'
    ),
    (
        'Luna',
        'CAT',
        'Domestic Shorthair',
        10,
        'GOOD',
        'CALM',
        FALSE,
        'ANY',
        'AVAILABLE'
    ),
    (
        'Rocky',
        'DOG',
        'Mixed Breed',
        36,
        'RECOVERING',
        'SHY',
        TRUE,
        'HOUSE',
        'AVAILABLE'
    )
ON CONFLICT DO NOTHING;

INSERT INTO
    foster_families (
        family_name,
        contact_name,
        phone,
        housing_type,
        experience_level,
        capacity,
        active_pet_count,
        has_yard,
        has_children,
        is_active
    )
VALUES (
        'Nguyen Family',
        'Minh Nguyen',
        '0900000001',
        'HOUSE',
        'EXPERIENCED',
        3,
        0,
        TRUE,
        FALSE,
        TRUE
    ),
    (
        'Tran Family',
        'Lan Tran',
        '0900000002',
        'APARTMENT',
        'BEGINNER',
        2,
        0,
        FALSE,
        TRUE,
        TRUE
    ),
    (
        'Le Family',
        'An Le',
        '0900000003',
        'HOUSE',
        'INTERMEDIATE',
        1,
        0,
        TRUE,
        FALSE,
        TRUE
    )
ON CONFLICT DO NOTHING;