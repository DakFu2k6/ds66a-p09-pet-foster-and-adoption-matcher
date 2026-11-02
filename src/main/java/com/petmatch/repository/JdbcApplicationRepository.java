package com.petmatch.repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.petmatch.config.DatabaseConnection;
import com.petmatch.model.AdoptionApplication;
import com.petmatch.model.ApplicationStatus;

public class JdbcApplicationRepository implements ApplicationRepository {

    private AdoptionApplication mapRow(ResultSet r) throws SQLException {
        AdoptionApplication app = new AdoptionApplication();
        app.setId(r.getInt("application_id"));
        app.setStatus(ApplicationStatus.valueOf(r.getString("status")));
        
        Date sqlDate = r.getDate("application_date");
        if (sqlDate != null) {
            app.setApplicationDate(sqlDate.toLocalDate());
        }
        return app;
    }

    @Override
    public AdoptionApplication save(long petId, long familyId, String status) {
        String q = "INSERT INTO applications(pet_id, family_id, status, application_date) VALUES(?, ?, ?, ?)";
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(q, Statement.RETURN_GENERATED_KEYS)) {
            
            p.setLong(1, petId);
            p.setLong(2, familyId);
            p.setString(3, status);
            p.setDate(4, Date.valueOf(LocalDate.now()));
            p.executeUpdate();

            try (ResultSet r = p.getGeneratedKeys()) {
                if (r.next()) {
                    AdoptionApplication app = new AdoptionApplication();
                    app.setId((int) r.getLong(1));
                    app.setStatus(ApplicationStatus.valueOf(status));
                    app.setApplicationDate(LocalDate.now());
                    return app;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không thể lưu đơn đăng ký vào Database", e);
        }
        return null;
    }

    @Override
    public List<AdoptionApplication> findAll() {
        String q = "SELECT * FROM applications ORDER BY application_id DESC";
        List<AdoptionApplication> list = new ArrayList<>();
        
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(q);
             ResultSet r = p.executeQuery()) {
            
            while (r.next()) {
                list.add(mapRow(r)); 
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không thể đọc danh sách đơn đăng ký", e);
        }
        return list;
    }

    @Override
    public Optional<AdoptionApplication> findById(long id) {
        String q = "SELECT * FROM applications WHERE application_id = ?";
        
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(q)) {
            
            p.setLong(1, id);
            try (ResultSet r = p.executeQuery()) {
                if (r.next()) {
                    return Optional.of(mapRow(r)); 
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Không tìm thấy đơn đăng ký mã số: " + id, e);
        }
        return Optional.empty();
    }

    @Override
    public void update(AdoptionApplication app) {
        String q = "UPDATE applications SET status = ? WHERE application_id = ?";
        
        try (Connection c = DatabaseConnection.getConnection();
             PreparedStatement p = c.prepareStatement(q)) {
            
            p.setString(1, app.getStatus().name());
            p.setInt(2, app.getId());
            p.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Không thể cập nhật trạng thái đơn đăng ký", e);
        }
    }
}