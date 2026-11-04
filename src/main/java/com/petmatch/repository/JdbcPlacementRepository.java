package com.petmatch.repository;

import com.petmatch.config.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class JdbcPlacementRepository implements PlacementRepository {

    @Override
    public com.petmatch.model.Placement createPlacement(
            long applicationId, long petId, long familyId) {
        String sqlCheckApp = "SELECT status FROM applications WHERE application_id = ? AND pet_id = ? AND family_id = ?"
                + " FOR UPDATE";
        String sqlCheckPet = "SELECT * FROM pets WHERE pet_id = ? FOR UPDATE";
        String sqlCheckFamily = "SELECT * FROM foster_families WHERE family_id = ? FOR UPDATE";

        long placementId = 0;
        String sqlInsertPlacement = "INSERT INTO placements(application_id, pet_id, family_id) VALUES(?, ?, ?)";
        String sqlUpdatePetStatus = "UPDATE pets SET status = 'PLACED' WHERE pet_id = ?";
        String sqlUpdateFamilyCount = "UPDATE foster_families SET active_pet_count = active_pet_count + 1 WHERE family_id = ?";
        String sqlUpdateAppPlaced = "UPDATE applications SET status = 'PLACED' WHERE application_id = ?";
        String sqlRejectOtherApps = "UPDATE applications SET status = 'REJECTED' WHERE (pet_id = ? OR family_id = "
                + familyId
                + ") AND application_id <> ? AND status IN ('PENDING', 'APPROVED')";

        try (Connection c = DatabaseConnection.getConnection()) {
            c.setAutoCommit(false);

            try {
                com.petmatch.model.Pet lockedPet;
                com.petmatch.model.FosterFamily lockedFamily;
                try (PreparedStatement p = c.prepareStatement(sqlCheckApp)) {
                    p.setLong(1, applicationId);
                    p.setLong(2, petId);
                    p.setLong(3, familyId);
                    try (ResultSet rs = p.executeQuery()) {
                        if (!rs.next()) {
                            throw new SQLException(
                                    "Không tìm thấy đơn đăng ký hoặc thông tin liên kết không khớp.");
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
                        if (activeCount != 0)
                            throw new SQLException("Family is already committed.");
                        lockedFamily = JdbcFosterFamilyRepository.mapRow(rs);
                        if (activeCount >= capacity) {
                            throw new SQLException("Gia đình nhận nuôi đã đạt giới hạn số lượng thú cưng.");
                        }
                    }
                }

                try (PreparedStatement q = c.prepareStatement(
                        "SELECT p.*, cp.care_profile_id, cp.daily_exercise_minutes, cp.feeding_notes,"
                                + " cp.medical_notes FROM pets p LEFT JOIN care_profiles cp ON"
                                + " cp.pet_id=p.pet_id WHERE p.pet_id=?")) {
                    q.setLong(1, petId);
                    try (ResultSet rs = q.executeQuery()) {
                        if (!rs.next())
                            throw new SQLException("Pet missing");
                        lockedPet = JdbcPetRepository.mapRow(rs);
                    }
                }
                var result = new com.petmatch.service.MatchingService(JdbcCriteriaRepository.load(c))
                        .evaluateMatch(lockedPet, lockedFamily, com.petmatch.model.MatchOperator.AND);
                if (!result.isSatisfied())
                    throw new SQLException("Handover requires all care criteria: " + result.getReason());
                JdbcEvaluationRepository.save(
                        c, applicationId, com.petmatch.model.MatchOperator.AND, lockedPet, lockedFamily);

                try (PreparedStatement p = c.prepareStatement(sqlInsertPlacement,
                        java.sql.Statement.RETURN_GENERATED_KEYS)) {
                    p.setLong(1, applicationId);
                    p.setLong(2, petId);
                    p.setLong(3, familyId);
                    p.executeUpdate();
                    try (var keys = p.getGeneratedKeys()) {
                        if (!keys.next())
                            throw new SQLException("Missing placement ID");
                        placementId = keys.getLong(1);
                    }
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

                java.time.LocalDateTime date;
                try (var q = c.prepareStatement("SELECT placement_date FROM placements WHERE placement_id=?")) {
                    q.setLong(1, placementId);
                    try (var rs = q.executeQuery()) {
                        if (!rs.next())
                            throw new SQLException("Placement missing");
                        date = rs.getTimestamp(1).toLocalDateTime();
                    }
                }
                c.commit();
                return new com.petmatch.model.Placement(
                        placementId, applicationId, petId, familyId, date, "ACTIVE");
            } catch (SQLException | RuntimeException ex) {
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
