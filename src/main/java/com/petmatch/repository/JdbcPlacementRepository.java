package com.petmatch.repository;

import com.petmatch.config.DatabaseConnection;
import java.sql.*;

public class JdbcPlacementRepository implements PlacementRepository {
    public void createPlacement(long applicationId, long petId, long familyId) {
        String lockPet = "SELECT status,housing_requirement,special_care_required FROM pets WHERE pet_id=? FOR UPDATE";
        String lockFamily = "SELECT capacity,active_pet_count,is_active,housing_type,experience_level,has_yard FROM foster_families WHERE family_id=? FOR UPDATE";
        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                String housingRequirement;
                boolean specialCare;
                try (PreparedStatement p = c.prepareStatement(
                        "SELECT pet_id,family_id,status FROM applications WHERE application_id=? FOR UPDATE")) {
                    p.setLong(1, applicationId);
                    try (ResultSet rs = p.executeQuery()) {
                        if (!rs.next() || rs.getLong(1) != petId || rs.getLong(2) != familyId
                                || !java.util.Set.of("PENDING", "APPROVED").contains(rs.getString(3)))
                            throw new SQLException("Application identity or state is invalid.");
                    }
                }
                try (PreparedStatement p = c.prepareStatement(lockPet)) {
                    p.setLong(1, petId);
                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next() || !"AVAILABLE".equals(r.getString(1)))
                            throw new SQLException("Pet is not available.");
                        housingRequirement = r.getString("housing_requirement");
                        specialCare = r.getBoolean("special_care_required");
                    }
                }
                try (PreparedStatement p = c.prepareStatement(lockFamily)) {
                    p.setLong(1, familyId);
                    try (ResultSet r = p.executeQuery()) {
                        if (!r.next() || !r.getBoolean("is_active")
                                || r.getInt("active_pet_count") >= r.getInt("capacity"))
                            throw new SQLException("Family is inactive or at capacity.");
                        String housing = r.getString("housing_type");
                        boolean compatible = "ANY".equals(housingRequirement)
                                || "HOUSE".equals(housingRequirement) && !"APARTMENT".equals(housing)
                                || "APARTMENT".equals(housingRequirement) && "APARTMENT".equals(housing)
                                || "YARD".equals(housingRequirement) && r.getBoolean("has_yard");
                        if (!compatible || specialCare && "BEGINNER".equals(r.getString("experience_level")))
                            throw new SQLException("Matching changed: housing or experience is incompatible.");
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
                try (PreparedStatement p = c.prepareStatement(
                        "UPDATE applications SET status='REJECTED' WHERE pet_id=? AND application_id<>? AND status IN ('PENDING','APPROVED')")) {
                    p.setLong(1, petId);
                    p.setLong(2, applicationId);
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
