package com.petmatch.repository;

import com.petmatch.config.DatabaseConnection;
import com.petmatch.model.AdoptionApplication;
import com.petmatch.model.ApplicationStatus;
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

public class JdbcApplicationRepository implements ApplicationRepository {

    private final PetRepository petRepository = new JdbcPetRepository();
    private final FosterFamilyRepository familyRepository = new JdbcFosterFamilyRepository();

    private AdoptionApplication mapRow(ResultSet r) throws SQLException {
        return mapRow(r, null, null);
    }

    private AdoptionApplication mapRow(
            ResultSet r,
            java.util.Map<Long, com.petmatch.model.Pet> pets,
            java.util.Map<Long, com.petmatch.model.FosterFamily> families)
            throws SQLException {
        AdoptionApplication app = new AdoptionApplication();
        app.setId(r.getLong("application_id"));
        app.setStatus(ApplicationStatus.valueOf(r.getString("status")));

        Date sqlDate = r.getDate("application_date");
        if (sqlDate != null) {
            app.setApplicationDate(sqlDate.toLocalDate());
        }

        long petId = r.getLong("pet_id");
        long familyId = r.getLong("family_id");

        if (pets == null) {
            petRepository.findById(petId).ifPresent(app::setPet);
            familyRepository.findById(familyId).ifPresent(app::setFamily);
        } else {
            app.setPet(pets.get(petId));
            app.setFamily(families.get(familyId));
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
                    app.setId(r.getLong(1));
                    app.setStatus(ApplicationStatus.valueOf(status));
                    app.setApplicationDate(LocalDate.now());

                    petRepository.findById(petId).ifPresent(app::setPet);
                    familyRepository.findById(familyId).ifPresent(app::setFamily);

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
        var pets = petRepository.findAll().stream()
                .collect(java.util.stream.Collectors.toMap(com.petmatch.model.Pet::getId, p -> p));
        var families = familyRepository.findAll().stream()
                .collect(
                        java.util.stream.Collectors.toMap(com.petmatch.model.FosterFamily::getId, f -> f));

        try (Connection c = DatabaseConnection.getConnection();
                PreparedStatement p = c.prepareStatement(q);
                ResultSet r = p.executeQuery()) {

            while (r.next()) {
                list.add(mapRow(r, pets, families));
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
        if (app.getStatus() == ApplicationStatus.APPROVED) {
            var current = findById(app.getId()).orElseThrow();
            if (!new com.petmatch.service.MatchingService(JdbcCriteriaRepository.load())
                    .evaluateMatch(
                            current.getPet(), current.getFamily(), com.petmatch.model.MatchOperator.AND)
                    .isSatisfied())
                throw new IllegalArgumentException("Current matching requirements fail.");
        }
        String q = "UPDATE applications SET status = ? WHERE application_id = ? AND status IN"
                + " ('PENDING','APPROVED')";

        try (Connection c = DatabaseConnection.getConnection();
                PreparedStatement p = c.prepareStatement(q)) {

            p.setString(1, app.getStatus().name());
            p.setLong(2, app.getId());
            if (p.executeUpdate() != 1)
                throw new SQLException("Application was changed or is already final.");
        } catch (SQLException e) {
            throw new RuntimeException("Không thể cập nhật trạng thái đơn đăng ký", e);
        }
    }
}
