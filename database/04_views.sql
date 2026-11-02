USE pet_matcher_db;

CREATE OR REPLACE VIEW v_dashboard_summary AS
SELECT (
        SELECT COUNT(*)
        FROM pets
    ) AS total_pets,
    (
        SELECT COUNT(*)
        FROM pets
        WHERE
            status = 'AVAILABLE'
    ) AS available_pets,
    (
        SELECT COUNT(*)
        FROM foster_families
        WHERE
            is_active = TRUE
    ) AS active_families,
    (
        SELECT COUNT(*)
        FROM applications
        WHERE
            status = 'PENDING'
    ) AS pending_applications,
    (
        SELECT COUNT(*)
        FROM placements
        WHERE
            status = 'ACTIVE'
    ) AS active_placements;