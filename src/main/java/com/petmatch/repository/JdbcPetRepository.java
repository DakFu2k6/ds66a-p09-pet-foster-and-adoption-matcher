package com.petmatch.repository;

import com.petmatch.config.DatabaseConnection;
import com.petmatch.model.*;
import java.sql.*;
import java.util.*;

public class JdbcPetRepository implements PetRepository {
    private Pet map(ResultSet rs) throws SQLException {
        PetType type = PetType.valueOf(rs.getString("species"));
        Object[] a = { rs.getLong("pet_id"), rs.getString("name"), rs.getString("breed"), rs.getInt("age_months"),
                rs.getString("health_status"), rs.getString("temperament"), rs.getBoolean("special_care_required"),
                rs.getString("housing_requirement"), PetStatus.valueOf(rs.getString("status")), null };
        if (type == PetType.DOG)
            return new Dog((long) a[0], (String) a[1], (String) a[2], (int) a[3], (String) a[4], (String) a[5],
                    (boolean) a[6], (String) a[7], (PetStatus) a[8], null);
        return new Cat((long) a[0], (String) a[1], (String) a[2], (int) a[3], (String) a[4], (String) a[5],
                (boolean) a[6], (String) a[7], (PetStatus) a[8], null);
    }

    public List<Pet> findAll() {
        String sql = "SELECT * FROM pets ORDER BY pet_id DESC";
        try (Connection c = DatabaseConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql);
                ResultSet rs = ps.executeQuery()) {
            List<Pet> list = new ArrayList<>();
            while (rs.next())
                list.add(map(rs));
            return list;
        } catch (SQLException e) {
            throw new RuntimeException("Could not load pets", e);
        }
    }

    public Optional<Pet> findById(long id) {
        try (Connection c = DatabaseConnection.getConnection();
                PreparedStatement ps = c.prepareStatement("SELECT * FROM pets WHERE pet_id=?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not find pet", e);
        }
    }

    // MySQL không có RETURNING -> dùng RETURN_GENERATED_KEYS để lấy id tự tăng.
    public Pet save(Pet p) {
        String sql = "INSERT INTO pets(name,species,breed,age_months,health_status,temperament,special_care_required,housing_requirement,status) VALUES(?,?,?,?,?,?,?,?,?)";
        try (Connection c = DatabaseConnection.getConnection();
                PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, p);
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next())
                    throw new SQLException("No generated id returned.");
                p.setId(rs.getLong(1));
                return p;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Could not save pet", e);
        }
    }

    public void update(Pet p) {
        String sql = "UPDATE pets SET name=?,species=?,breed=?,age_months=?,health_status=?,temperament=?,special_care_required=?,housing_requirement=?,status=? WHERE pet_id=?";
        try (Connection c = DatabaseConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, p);
            ps.setLong(10, p.getId());
            if (ps.executeUpdate() == 0)
                throw new RuntimeException("Pet not found.");
        } catch (SQLException e) {
            throw new RuntimeException("Could not update pet", e);
        }
    }

    private void bind(PreparedStatement ps, Pet p) throws SQLException {
        ps.setString(1, p.getName());
        ps.setString(2, p.getType().name());
        ps.setString(3, p.getBreed());
        ps.setInt(4, p.getAgeMonths());
        ps.setString(5, p.getHealthStatus());
        ps.setString(6, p.getTemperament());
        ps.setBoolean(7, p.isSpecialCareRequired());
        ps.setString(8, p.getHousingRequirement());
        ps.setString(9, p.getStatus().name());
    }

    public void delete(long id) {
        try (Connection c = DatabaseConnection.getConnection();
                PreparedStatement ps = c.prepareStatement("DELETE FROM pets WHERE pet_id=?")) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Could not delete pet. It may be referenced by an application.", e);
        }
    }
}