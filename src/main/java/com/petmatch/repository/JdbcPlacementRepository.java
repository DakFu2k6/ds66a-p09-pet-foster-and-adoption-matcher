package com.petmatch.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.petmatch.config.DatabaseConnection;

public class JdbcPlacementRepository implements PlacementRepository {

    @Override
    public void createPlacement(long applicationId, long petId, long familyId) {
        String sqlCheckApp = "SELECT status FROM applications WHERE application_id = ? AND pet_id = ? AND family_id = ? FOR UPDATE";
        String sqlCheckPet = "SELECT status FROM pets WHERE pet_id = ? FOR UPDATE";
        String sqlCheckFamily = "SELECT is_active, active_pet_count, capacity FROM foster_families WHERE family_id = ? FOR UPDATE";
        
        String sqlInsertPlacement = "INSERT INTO placements(application_id, pet_id, family_id) VALUES(?, ?, ?)";
        String sqlUpdatePetStatus = "UPDATE pets SET status = 'PLACED' WHERE pet_id = ?";
        String sqlUpdateFamilyCount = "UPDATE foster_families SET active_pet_count = active_pet_count + 1 WHERE family_id = ?";
        String sqlUpdateAppPlaced = "UPDATE applications SET status = 'PLACED' WHERE application_id = ?";
        String sqlRejectOtherApps = "UPDATE applications SET status = 'REJECTED' WHERE pet_id = ? AND application_id <> ? AND status IN ('PENDING', 'APPROVED')";

        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false); 

            try {
                try (PreparedStatement p = c.prepareStatement(sqlCheckApp)) {
                    p.setLong(1, applicationId);
                    p.setLong(2, petId);
                    p.setLong(3, familyId);
                    try (ResultSet rs = p.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Không tìm thấy đơn đăng ký hoặc thông tin liên kết không khớp.");
                        }
                        String status = rs.getString("status");
                        if (!"PENDING".equals(status) && !"APPROVED".equals(status)) {
                            throw new SQLException("Trạng thái đơn đăng ký không hợp lệ để nhận nuôi.");
                        }
                    }
                }

                try (PreparedStatement p = c.prepareStatement(sqlCheckPet)) {
                    p.setLong(1, petId);
                    try (ResultSet rs = p.executeQuery()) {
                        if (!rs.next() || !"AVAILABLE".equals(rs.getString("status"))) {
                            throw new SQLException("Thú cưng này hiện không còn khả dụng.");
                        }
                    }
                }

                try (PreparedStatement p = c.prepareStatement(sqlCheckFamily)) {
                    p.setLong(1, familyId);
                    try (ResultSet rs = p.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException("Không tìm thấy thông tin gia đình nhận nuôi.");
                        }
                        boolean isActive = rs.getBoolean("is_active");
                        int activeCount = rs.getInt("active_pet_count");
                        int capacity = rs.getInt("capacity");

                        if (!isActive) {
                            throw new SQLException("Gia đình nhận nuôi hiện đang tạm ngưng hoạt động.");
                        }
                        if (activeCount >= capacity) {
                            throw new SQLException("Gia đình nhận nuôi đã đạt giới hạn số lượng thú cưng.");
                        }
                    }
                }

                try (PreparedStatement p = c.prepareStatement(sqlInsertPlacement)) {
                    p.setLong(1, applicationId);
                    p.setLong(2, petId);
                    p.setLong(3, familyId);
                    p.executeUpdate();
                }

                try (PreparedStatement p = c.prepareStatement(sqlUpdatePetStatus)) {
                    p.setLong(1, petId);
                    p.executeUpdate();
                }

                try (PreparedStatement p = c.prepareStatement(sqlUpdateFamilyCount)) {
                    p.setLong(1, familyId);
                    p.executeUpdate();
                }

                try (PreparedStatement p = c.prepareStatement(sqlUpdateAppPlaced)) {
                    p.setLong(1, applicationId);
                    p.executeUpdate();
                }

                try (PreparedStatement p = c.prepareStatement(sqlRejectOtherApps)) {
                    p.setLong(1, petId);
                    p.setLong(2, applicationId);
                    p.executeUpdate();
                }

                c.commit(); 
            } catch (SQLException ex) {
                c.rollback(); 
                throw ex;
            } finally {
                c.setAutoCommit(true); 
            }

        } catch (SQLException e) {
            throw new RuntimeException("Giao dịch ghép nối thất bại: " + e.getMessage(), e);
        }
    }
}