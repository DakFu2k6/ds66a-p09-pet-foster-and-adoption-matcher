package com.petmatch.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.petmatch.config.DatabaseConnection;
import com.petmatch.model.CareProfile;
import com.petmatch.model.Cat;
import com.petmatch.model.Dog;
import com.petmatch.model.Pet;
import com.petmatch.model.PetStatus;
import com.petmatch.model.PetType;

public class JdbcPetRepository implements PetRepository {

    private Pet mapRow(ResultSet rs) throws SQLException {
        long id = rs.getLong("pet_id");
        String name = rs.getString("name");
        String breed = rs.getString("breed");
        int ageMonths = rs.getInt("age_months");
        String healthStatus = rs.getString("health_status");
        String temperament = rs.getString("temperament");
        boolean specialCare = rs.getBoolean("special_care_required");
        String housingReq = rs.getString("housing_requirement");
        PetStatus status = PetStatus.valueOf(rs.getString("status"));
        String species = rs.getString("species");

        CareProfile care = null;
        if (rs.getObject("care_profile_id") != null) {
            care = new CareProfile(
                rs.getLong("care_profile_id"),
                rs.getInt("daily_exercise_minutes"),
                rs.getString("feeding_notes"),
                rs.getString("medical_notes")
            );
        }

        if ("DOG".equalsIgnoreCase(species) || PetType.DOG.name().equalsIgnoreCase(species)) {
            return new Dog(id, name, breed, ageMonths, healthStatus, temperament, specialCare, housingReq, status, care);
        } else {
            return new Cat(id, name, breed, ageMonths, healthStatus, temperament, specialCare, housingReq, status, care);
        }
    }

    @Override
    public List<Pet> findAll() {
        String sql = "SELECT p.*, c.care_profile_id, c.daily_exercise_minutes, c.feeding_notes, c.medical_notes " +
                     "FROM pets p LEFT JOIN care_profiles c ON c.pet_id = p.pet_id ORDER BY p.pet_id DESC";
        
        List<Pet> list = new ArrayList<>();
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không thể tải danh sách thú cưng", e);
        }
        return list;
    }

    @Override
    public Optional<Pet> findById(long id) {
        String sql = "SELECT p.*, c.care_profile_id, c.daily_exercise_minutes, c.feeding_notes, c.medical_notes " +
                     "FROM pets p LEFT JOIN care_profiles c ON c.pet_id = p.pet_id WHERE p.pet_id = ?";
        
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không tìm thấy thú cưng với mã số: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public Pet save(Pet p) {
        String sql = "INSERT INTO pets(name, species, breed, age_months, health_status, temperament, special_care_required, housing_requirement, status) VALUES(?,?,?,?,?,?,?,?,?)";
        
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            bind(ps, p);
            ps.executeUpdate();
            
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    p.setId(rs.getLong(1));
                    return p;
                }
                throw new SQLException("Không thể lấy ID tự tăng sau khi lưu thú cưng.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không thể lưu thông tin thú cưng", e);
        }
    }

    @Override
    public void update(Pet p) {
        String sql = "UPDATE pets SET name=?, species=?, breed=?, age_months=?, health_status=?, temperament=?, special_care_required=?, housing_requirement=?, status=? WHERE pet_id=?";
        
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            
            bind(ps, p);
            ps.setLong(10, p.getId());
            
            if (ps.executeUpdate() == 0) {
                throw new RuntimeException("Không tìm thấy thú cưng để cập nhật.");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không thể cập nhật thông tin thú cưng", e);
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

    @Override
    public void delete(long id) {
        String sql = "DELETE FROM pets WHERE pet_id = ?";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Không thể xóa thú cưng. Thú cưng này có thể đang liên kết với đơn đăng ký.", e);
        }
    }
}