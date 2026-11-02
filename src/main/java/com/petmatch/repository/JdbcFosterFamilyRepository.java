package com.petmatch.repository;

import com.petmatch.config.DatabaseConnection;
import com.petmatch.model.*;
import java.sql.*;
import java.util.*;

public class JdbcFosterFamilyRepository implements FosterFamilyRepository {
    private FosterFamily map(ResultSet r) throws SQLException {
        return new FosterFamily(r.getLong("family_id"), r.getString("family_name"), r.getString("contact_name"),
                r.getString("phone"), HousingType.valueOf(r.getString("housing_type")),
                ExperienceLevel.valueOf(r.getString("experience_level")), r.getInt("capacity"),
                r.getInt("active_pet_count"), r.getBoolean("has_yard"), r.getBoolean("has_children"),
                r.getBoolean("is_active"));
    }

    public List<FosterFamily> findAll() {
        try (Connection c = DatabaseConnection.getConnection();
                PreparedStatement p = c.prepareStatement("SELECT * FROM foster_families ORDER BY family_id DESC");
                ResultSet r = p.executeQuery()) {
            List<FosterFamily> x = new ArrayList<>();
            while (r.next())
                x.add(map(r));
            return x;
        } catch (SQLException e) {
            throw new RuntimeException("Could not load families", e);
        }
    }

    public Optional<FosterFamily> findById(long id) {
        try (Connection c = DatabaseConnection.getConnection();
                PreparedStatement p = c.prepareStatement("SELECT * FROM foster_families WHERE family_id=?")) {
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) {
                return r.next() ? Optional.of(map(r)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not find family", e);
        }
    }

    public FosterFamily save(FosterFamily f) {
        String q = "INSERT INTO foster_families(family_name,contact_name,phone,housing_type,experience_level,capacity,active_pet_count,has_yard,has_children,is_active) VALUES(?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
                PreparedStatement p = c.prepareStatement(q, Statement.RETURN_GENERATED_KEYS)) {
            p.setString(1, f.getFamilyName());
            p.setString(2, f.getContactName());
            p.setString(3, f.getPhone());
            p.setString(4, f.getHousingType().name());
            p.setString(5, f.getExperienceLevel().name());
            p.setInt(6, f.getCapacity());
            p.setInt(7, f.getActivePetCount());
            p.setBoolean(8, f.isHasYard());
            p.setBoolean(9, f.isHasChildren());
            p.setBoolean(10, f.isActive());
            p.executeUpdate();
            try (ResultSet r = p.getGeneratedKeys()) {
                if (!r.next())
                    throw new SQLException("No generated id returned.");
                return new FosterFamily(r.getLong(1), f.getFamilyName(), f.getContactName(), f.getPhone(),
                        f.getHousingType(), f.getExperienceLevel(), f.getCapacity(), f.getActivePetCount(),
                        f.isHasYard(), f.isHasChildren(), f.isActive());
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not save family", e);
        }
    }
}