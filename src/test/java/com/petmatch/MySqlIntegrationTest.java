package com.petmatch;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import com.petmatch.config.DatabaseConnection;
import com.petmatch.repository.JdbcPlacementRepository;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;

@EnabledIfEnvironmentVariable(named = "PETMATCH_MYSQL_TEST", matches = "true")
class MySqlIntegrationTest {
    long insert(Connection c, String sql) throws SQLException {
        try (Statement s = c.createStatement()) {
            s.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
            try (ResultSet r = s.getGeneratedKeys()) {
                r.next();
                return r.getLong(1);
            }
        }
    }

    long scalar(Connection c, String sql) throws SQLException {
        try (Statement s = c.createStatement(); ResultSet r = s.executeQuery(sql)) {
            r.next();
            return r.getLong(1);
        }
    }

    @Test
    void placementIsAtomicAndRejectsDuplicate() throws Exception {
        try (Connection c = DatabaseConnection.getConnection()) {
            assertTrue(c.getCatalog().endsWith("_test"), "Integration tests require DB_NAME ending _test");
            long p = 0, f = 0, a = 0;
            try {
                p = insert(c, "INSERT INTO pets(name,species,age_months) VALUES('Integration fixture','DOG',12)");
                f = insert(c,
                        "INSERT INTO foster_families(family_name,contact_name,phone,housing_type,experience_level,capacity) VALUES('Integration fixture','Test','0900000000','HOUSE','EXPERIENCED',1)");
                a = insert(c, "INSERT INTO applications(pet_id,family_id) VALUES(" + p + "," + f + ")");
                var repo = new JdbcPlacementRepository();
                repo.createPlacement(a, p, f);
                assertEquals(1, scalar(c, "SELECT active_pet_count FROM foster_families WHERE family_id=" + f));
                assertEquals(1, scalar(c, "SELECT COUNT(*) FROM pets WHERE pet_id=" + p + " AND status='PLACED'"));
                assertEquals(1, scalar(c,
                        "SELECT COUNT(*) FROM applications WHERE application_id=" + a + " AND status='PLACED'"));
                final long app = a, pet = p, family = f;
                assertThrows(RuntimeException.class, () -> repo.createPlacement(app, pet, family));
                assertEquals(1, scalar(c, "SELECT COUNT(*) FROM placements WHERE pet_id=" + p));
                assertEquals(1, scalar(c, "SELECT active_pet_count FROM foster_families WHERE family_id=" + f));
            } finally {
                try (Statement s = c.createStatement()) {
                    s.executeUpdate("DELETE FROM placements WHERE pet_id=" + p);
                    s.executeUpdate("DELETE FROM applications WHERE pet_id=" + p);
                    s.executeUpdate("DELETE FROM pets WHERE pet_id=" + p);
                    s.executeUpdate("DELETE FROM foster_families WHERE family_id=" + f);
                }
            }
        }
    }
}
