package com.petmatch;

import java.sql.SQLException;

import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

import com.petmatch.config.DatabaseConnection;
import com.petmatch.model.CareProfile;
import com.petmatch.model.Dog;
import com.petmatch.model.ExperienceLevel;
import com.petmatch.model.FosterFamily;
import com.petmatch.model.HousingType;
import com.petmatch.model.MatchOperator;
import com.petmatch.model.PetStatus;
import com.petmatch.repository.JdbcApplicationRepository;
import com.petmatch.repository.JdbcCriteriaRepository;
import com.petmatch.repository.JdbcEvaluationRepository;
import com.petmatch.repository.JdbcFosterFamilyRepository;
import com.petmatch.repository.JdbcPetRepository;
import com.petmatch.repository.JdbcPlacementRepository;
import com.petmatch.service.ApplicationService;

@EnabledIfEnvironmentVariable(named = "PETMATCH_MYSQL_TEST", matches = "true")
class MySqlWorkflowIntegrationTest {
    long pet, family, application;

    @BeforeEach
    @SuppressWarnings("unused")
    void setup() throws Exception {
        try (var c = DatabaseConnection.getConnection()) {
            assertTrue(c.getCatalog().endsWith("_test"), "Only isolated _test database allowed");
        }
        var p = new JdbcPetRepository()
                .save(
                        new Dog(
                                0,
                                "Regression fixture",
                                "Mix",
                                12,
                                "GOOD",
                                "CALM",
                                false,
                                "ANY",
                                PetStatus.AVAILABLE,
                                new CareProfile(0, 80, "Fresh food", "Medical note")));
        pet = p.getId();
        var f = new JdbcFosterFamilyRepository()
                .save(
                        new FosterFamily(
                                0,
                                "Regression fixture",
                                "Contact",
                                "0900000000",
                                HousingType.HOUSE,
                                ExperienceLevel.EXPERIENCED,
                                3,
                                0,
                                true,
                                false,
                                true));
        family = f.getId();
        application = new ApplicationService(new JdbcApplicationRepository()).createApplication(p, f).getId();
    }

    @AfterEach
    @SuppressWarnings("unused")
    void cleanup() throws Exception {
        try (var c = DatabaseConnection.getConnection();
                var q = c.createStatement()) {
            q.executeUpdate("DELETE FROM placements WHERE pet_id=" + pet + " OR family_id=" + family);
            q.executeUpdate("DELETE FROM applications WHERE pet_id=" + pet + " OR family_id=" + family);
            q.executeUpdate("DELETE FROM pets WHERE pet_id=" + pet);
            q.executeUpdate("DELETE FROM foster_families WHERE family_id=" + family);
        }
    }

    long count(String sql) throws Exception {
        try (var c = DatabaseConnection.getConnection();
                var q = c.createStatement();
                var rs = q.executeQuery(sql)) {
            rs.next();
            return rs.getLong(1);
        }
    }

    @Test
    void careProfilePersistsAndUpdates() {
        var repo = new JdbcPetRepository();
        var p = repo.findById(pet).orElseThrow();
        assertEquals(80, p.getCareProfile().getDailyExerciseMinutes());
        assertEquals("Medical note", p.getCareProfile().getMedicalNotes());
        p.getCareProfile().setDailyExerciseMinutes(90);
        repo.update(p);
        assertEquals(90, repo.findById(pet).orElseThrow().getCareProfile().getDailyExerciseMinutes());
    }

    @Test
    void failedMatchRollsBackEverything() throws Exception {
        var repo = new JdbcPetRepository();
        var p = repo.findById(pet).orElseThrow();
        p.setHousingRequirement("APARTMENT");
        repo.update(p);
        var exception = assertThrows(
                RuntimeException.class,
                () -> new JdbcPlacementRepository().createPlacement(application, pet, family));
        assertNotNull(exception);
        assertEquals(0, count("SELECT COUNT(*) FROM placements WHERE pet_id=" + pet));
        assertEquals(
                0, count("SELECT active_pet_count FROM foster_families WHERE family_id=" + family));
        assertEquals(PetStatus.AVAILABLE, repo.findById(pet).orElseThrow().getStatus());
    }

    @Test
    void committedFamilyRejectsSecondPet() throws Exception {
        new JdbcPlacementRepository().createPlacement(application, pet, family);
        try (var c = DatabaseConnection.getConnection();
                var q = c.prepareStatement("INSERT INTO applications(pet_id,family_id) VALUES(?,?)")) {
            q.setLong(1, pet);
            q.setLong(2, family);
            var exception = assertThrows(SQLException.class, q::executeUpdate);
            assertNotNull(exception.getMessage());
        }
        assertEquals(
                1, count("SELECT active_pet_count FROM foster_families WHERE family_id=" + family));
    }

    @Test
    void evaluationAndCriteriaReload() {
        var p = new JdbcPetRepository().findById(pet).orElseThrow();
        var f = new JdbcFosterFamilyRepository().findById(family).orElseThrow();
        assertEquals(3, JdbcCriteriaRepository.load().size());
        JdbcEvaluationRepository.record(application, MatchOperator.OR, p, f);
        assertTrue(JdbcEvaluationRepository.latest(application).contains("OR"));
    }

    @Test
    void staleProfileCannotResetCommittedPet() {
        var repo = new JdbcPetRepository();
        var stale = repo.findById(pet).orElseThrow();
        new JdbcPlacementRepository().createPlacement(application, pet, family);
        var exception = assertThrows(RuntimeException.class, () -> repo.update(stale));
        assertNotNull(exception);
        assertEquals(PetStatus.PLACED, repo.findById(pet).orElseThrow().getStatus());
    }
}
