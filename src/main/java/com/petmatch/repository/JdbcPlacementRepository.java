package main.java.com.petmatch.repository;

import main.java.com.petmatch.config.DatabaseConnection;
import java.sql.*;

public class JdbcPlacementRepository implements PlacementRepository {
    public void createPlacement(long applicationId, long petId, long familyId) {
        String lockPet = "SELECT status FROM pets WHERE pet_id=? FOR UPDATE";
        String lockFamily = "SELECT capacity,active_pet_count,is_active FROM foster_families WHERE family_id=? FOR UPDATE";
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                try (PreparedStatement p = c.prepareStatement(lockPet)) {
                    p.setLong(1, petId);
                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next() || !"AVAILABLE".equals(r.getString(1)))
                            throw new SQLException("Pet is not available.");
                    }
                }
                try (PreparedStatement p = c.prepareStatement(lockFamily)) {
                    p.setLong(1, familyId);
                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next() || !r.getBoolean("is_active")
                                || r.getInt("active_pet_count") >= r.getInt("capacity"))
                            throw new SQLException("Family is inactive or at capacity.");
                    }
                }
                try (PreparedStatement p = c
                        .prepareStatement("INSERT INTO placements(application_id,pet_id,family_id) VALUES(?,?,?)")) {
                    p.setLong(1, applicationId);
                    p.setLong(2, petId);
                    p.setLong(3, familyId);
                    p.executeUpdate();
                }
                try (PreparedStatement p = c.prepareStatement("UPDATE pets SET status='PLACED' WHERE pet_id=?")) {
                    p.setLong(1, petId);
                    p.executeUpdate();
                }
                try (PreparedStatement p = c.prepareStatement(
                        "UPDATE foster_families SET active_pet_count=active_pet_count+1 WHERE family_id=?")) {
                    p.setLong(1, familyId);
                    p.executeUpdate();
                }
                try (PreparedStatement p = c
                        .prepareStatement("UPDATE applications SET status='PLACED' WHERE application_id=?")) {
                    p.setLong(1, applicationId);
                    p.executeUpdate();
                }
                c.commit();
            } catch (Exception ex) {
                c.rollback();
                throw ex;
            } finally {
                c.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Placement transaction failed: " + e.getMessage(), e);
        }
    }
}
