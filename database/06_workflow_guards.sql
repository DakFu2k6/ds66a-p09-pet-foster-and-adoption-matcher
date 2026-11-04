USE pet_matcher_db;

DROP TRIGGER IF EXISTS pets_guard_update;

DROP TRIGGER IF EXISTS families_guard_update;

DROP TRIGGER IF EXISTS applications_guard_insert;

DROP TRIGGER IF EXISTS applications_guard_update;

DELIMITER /
/

CREATE TRIGGER pets_guard_update BEFORE UPDATE ON pets FOR EACH ROW
BEGIN
 IF OLD.status='PLACED' AND NEW.status<>'PLACED' THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Committed pets cannot be reopened from profile editing';
 END IF;
END
/
/

CREATE TRIGGER families_guard_update BEFORE UPDATE ON foster_families FOR EACH ROW
BEGIN
 IF NEW.active_pet_count < OLD.active_pet_count THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Occupancy is managed by placement transactions';
 END IF;
END
/
/

CREATE TRIGGER applications_guard_insert BEFORE INSERT ON applications FOR EACH ROW
BEGIN
 DECLARE pet_state VARCHAR(30);
 DECLARE family_active BOOLEAN;
 DECLARE occupied INT;
 DECLARE slots INT;
 SELECT status INTO pet_state FROM pets WHERE pet_id=NEW.pet_id FOR UPDATE;
 SELECT is_active,active_pet_count,capacity INTO family_active,occupied,slots
  FROM foster_families WHERE family_id=NEW.family_id FOR UPDATE;
 IF NEW.status<>'PENDING' OR pet_state<>'AVAILABLE' OR family_active=FALSE OR occupied<>0 OR occupied>=slots THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Application requires available pet and active uncommitted family';
 END IF;
 IF EXISTS(SELECT 1 FROM applications WHERE pet_id=NEW.pet_id AND family_id=NEW.family_id AND status IN ('PENDING','APPROVED')) THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Duplicate open application';
 END IF;
END
/
/

CREATE TRIGGER applications_guard_update BEFORE UPDATE ON applications FOR EACH ROW
BEGIN
 IF NEW.pet_id<>OLD.pet_id OR NEW.family_id<>OLD.family_id THEN
  SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Application links are immutable';
 END IF;
 IF NEW.status<>OLD.status THEN
  IF OLD.status NOT IN ('PENDING','APPROVED') OR NEW.status NOT IN ('APPROVED','REJECTED','CANCELLED','PLACED') THEN
   SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Invalid application transition';
  END IF;
  IF NEW.status='PLACED' AND NOT EXISTS(SELECT 1 FROM placements WHERE application_id=OLD.application_id AND pet_id=OLD.pet_id AND family_id=OLD.family_id AND status='ACTIVE') THEN
   SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT='Placement transaction required';
  END IF;
 END IF;
END
/
/

DELIMITER;