USE pet_matcher_db;

DROP PROCEDURE IF EXISTS ensure_family_commitment;

DELIMITER //

CREATE PROCEDURE ensure_family_commitment()
BEGIN
    IF NOT EXISTS(SELECT 1 FROM information_schema.columns WHERE table_schema = DATABASE() AND table_name = 'placements' AND column_name = 'active_family_id') THEN
        ALTER TABLE placements ADD COLUMN active_family_id BIGINT GENERATED ALWAYS AS (CASE WHEN status = 'ACTIVE' THEN family_id ELSE NULL END) STORED;
    END IF;
    
    IF NOT EXISTS(SELECT 1 FROM information_schema.statistics WHERE table_schema = DATABASE() AND table_name = 'placements' AND index_name = 'uq_active_family') THEN
        CREATE UNIQUE INDEX uq_active_family ON placements(active_family_id);
    END IF;
END//

DELIMITER ;

CALL ensure_family_commitment();

DROP PROCEDURE ensure_family_commitment;