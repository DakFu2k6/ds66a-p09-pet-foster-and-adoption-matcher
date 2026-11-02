package com.petmatch.repository;

import com.petmatch.config.DatabaseConnection;
import com.petmatch.model.*;
import java.sql.*;
import java.util.*;

public class JdbcApplicationRepository implements ApplicationRepository {
    private AdoptionApplication map(ResultSet r) throws SQLException {
        return new AdoptionApplication(r.getLong("application_id"), r.getLong("pet_id"), r.getLong("family_id"),
                r.getTimestamp("application_date").toLocalDateTime(), ApplicationStatus.valueOf(r.getString("status")),
                r.getString("notes"));
    }

    public List<AdoptionApplication> findAll() {
        try (Connection c = DatabaseConnection.getConnection();
                PreparedStatement p = c.prepareStatement("SELECT * FROM applications ORDER BY application_id DESC");
                ResultSet r = p.executeQuery()) {
            List<AdoptionApplication> x = new ArrayList<>();
            while (r.next())
                x.add(map(r));
            return x;
        } catch (SQLException e) {
            throw new RuntimeException("Could not load applications", e);
        }
    }

    public Optional<AdoptionApplication> findById(long id) {
        try (Connection c = DatabaseConnection.getConnection();
                PreparedStatement p = c.prepareStatement("SELECT * FROM applications WHERE application_id=?")) {
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? Optional.of(map(r)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not find application", e);
        }
    }

    public AdoptionApplication save(long petId, long familyId, String notes) {
        String q = "INSERT INTO applications(pet_id,family_id,notes) VALUES(?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
                PreparedStatement p = c.prepareStatement(q, Statement.RETURN_GENERATED_KEYS)) {
            p.setLong(1, petId);
            p.setLong(2, familyId);
            p.setString(3, notes);
            p.executeUpdate();
            long newId;
            try (ResultSet keys = p.getGeneratedKeys()) {
                if (!keys.next())
                    throw new SQLException("No generated id returned.");
                newId = keys.getLong(1);
            }
            try (PreparedStatement s = c.prepareStatement("SELECT * FROM applications WHERE application_id=?")) {
                s.setLong(1, newId);
                try (ResultSet r = s.executeQuery()) {
                    r.next();
                    return map(r);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not create application", e);
        }
    }

    public void updateStatus(long id, ApplicationStatus status) {
        try (Connection c = DatabaseConnection.getConnection();
                PreparedStatement p = c.prepareStatement("UPDATE applications SET status=? WHERE application_id=?")) {
            p.setString(1, status.name());
            p.setLong(2, id);
            if (p.executeUpdate() == 0)
                throw new RuntimeException("Application not found.");
        } catch (SQLException e) {
            throw new RuntimeException("Could not update application", e);
        }
    }
}