package com.petmatch.repository;

import com.petmatch.config.DatabaseConnection;
import com.petmatch.model.ExperienceLevel;
import com.petmatch.model.FosterFamily;
import com.petmatch.model.HousingType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcFosterFamilyRepository implements FosterFamilyRepository {

    static FosterFamily mapRow(ResultSet r) throws SQLException {
        return new FosterFamily(
                r.getLong("family_id"),
                r.getString("family_name"),
                r.getString("contact_name"),
                r.getString("phone"),
                HousingType.valueOf(r.getString("housing_type")),
                ExperienceLevel.valueOf(r.getString("experience_level")),
                r.getInt("capacity"),
                r.getInt("active_pet_count"),
                r.getBoolean("has_yard"),
                r.getBoolean("has_children"),
                r.getBoolean("is_active"));
    }

    @Override
    public List<FosterFamily> findAll() {
        String q = "SELECT * FROM foster_families ORDER BY family_id DESC";
        List<FosterFamily> list = new ArrayList<>();

        try (Connection c = DatabaseConnection.getConnection();
                PreparedStatement p = c.prepareStatement(q);
                ResultSet r = p.executeQuery()) {

            while (r.next()) {
                list.add(mapRow(r));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không thể tải danh sách gia đình nhận nuôi", e);
        }
        return list;
    }

    @Override
    public Optional<FosterFamily> findById(long id) {
        String q = "SELECT * FROM foster_families WHERE family_id = ?";

        try (Connection c = DatabaseConnection.getConnection();
                PreparedStatement p = c.prepareStatement(q)) {

            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) {
                if (r.next()) {
                    return Optional.of(mapRow(r));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không tìm thấy gia đình nhận nuôi với mã số: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public FosterFamily save(FosterFamily f) {
        String q = "INSERT INTO foster_families(family_name, contact_name, phone, housing_type,"
                + " experience_level, capacity, active_pet_count, has_yard, has_children, is_active)"
                + " VALUES(?,?,?,?,?,?,?,?,?,?)";

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
                if (r.next()) {
                    f.setId(r.getLong(1));
                    return f;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không thể lưu thông tin gia đình nhận nuôi", e);
        }
        return null;
    }

    @Override
    public void update(FosterFamily f) {
        String q = "UPDATE foster_families SET family_name=?, contact_name=?, phone=?, housing_type=?,"
                + " experience_level=?, capacity=?, active_pet_count=active_pet_count, has_yard=?,"
                + " has_children=?, is_active=? WHERE family_id=? AND active_pet_count <= ?";

        try (Connection c = DatabaseConnection.getConnection();
                PreparedStatement p = c.prepareStatement(q)) {

            p.setString(1, f.getFamilyName());
            p.setString(2, f.getContactName());
            p.setString(3, f.getPhone());
            p.setString(4, f.getHousingType().name());
            p.setString(5, f.getExperienceLevel().name());
            p.setInt(6, f.getCapacity());
            p.setBoolean(7, f.isHasYard());
            p.setBoolean(8, f.isHasChildren());
            p.setBoolean(9, f.isActive());
            p.setLong(10, f.getId());
            p.setInt(11, f.getCapacity());

            if (p.executeUpdate() != 1)
                throw new SQLException("Family missing or capacity below current occupancy.");
        } catch (SQLException e) {
            throw new RuntimeException("Không thể cập nhật thông tin gia đình nhận nuôi", e);
        }
    }
}
