USE pet_matcher_db;

DROP PROCEDURE IF EXISTS add_check_if_missing;

DELIMITER / /

CREATE PROCEDURE add_check_if_missing(IN tbl VARCHAR(64),IN constraint_name_arg VARCHAR(64),IN expression_sql VARCHAR(500))
BEGIN
 IF NOT EXISTS(SELECT 1 FROM information_schema.table_constraints WHERE constraint_schema=DATABASE() AND table_name=tbl AND constraint_name=constraint_name_arg) THEN
  SET @ddl=CONCAT('ALTER TABLE ',tbl,' ADD CONSTRAINT ',constraint_name_arg,' CHECK (',expression_sql,')');
  PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
 END IF;
END//

DELIMITER;

CALL add_check_if_missing (
    'pets',
    'chk_pet_status_v2',
    'status IN (''AVAILABLE'',''RESERVED'',''PLACED'',''UNAVAILABLE'')'
);

CALL add_check_if_missing (
    'pets',
    'chk_pet_housing_v2',
    'housing_requirement IN (''ANY'',''HOUSE'',''APARTMENT'',''YARD'')'
);

CALL add_check_if_missing (
    'foster_families',
    'chk_family_housing_v2',
    'housing_type IN (''HOUSE'',''APARTMENT'',''FARM'')'
);

CALL add_check_if_missing (
    'foster_families',
    'chk_family_experience_v2',
    'experience_level IN (''BEGINNER'',''INTERMEDIATE'',''EXPERIENCED'')'
);

DROP PROCEDURE add_check_if_missing;