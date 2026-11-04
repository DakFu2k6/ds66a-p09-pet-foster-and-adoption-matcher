package com.petmatch;

import static org.junit.jupiter.api.Assertions.*;

import com.petmatch.config.DatabaseConnection;
import com.petmatch.model.*;
import com.petmatch.repository.*;
import com.petmatch.service.*;
import java.sql.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;

@EnabledIfEnvironmentVariable(named = "PETMATCH_MYSQL_TEST", matches = "true")
class MySqlWorkflowIntegrationTest {
    long pet, family, application;

    @BeforeEach
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
        assertThrows(
                RuntimeException.class,
                () -> new JdbcPlacementRepository().createPlacement(application, pet, family));
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
            assertThrows(SQLException.class, q::executeUpdate);
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
        assertThrows(RuntimeException.class, () -> repo.update(stale));
        assertEquals(PetStatus.PLACED, repo.findById(pet).orElseThrow().getStatus());
    }
}
