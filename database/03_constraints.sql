CREATE INDEX IF NOT EXISTS idx_families_active ON foster_families (is_active);

CREATE INDEX IF NOT EXISTS idx_placements_family_status ON placements (family_id, status);