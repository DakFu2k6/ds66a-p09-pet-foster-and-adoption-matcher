package com.petmatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.petmatch.model.AdoptionApplication;
import com.petmatch.model.ApplicationStatus;
import com.petmatch.model.Dog;
import com.petmatch.model.ExperienceLevel;
import com.petmatch.model.FosterFamily;
import com.petmatch.model.HousingType;
import com.petmatch.model.Pet;
import com.petmatch.model.PetStatus;
import com.petmatch.repository.ApplicationRepository;
import com.petmatch.repository.FosterFamilyRepository;
import com.petmatch.repository.PetRepository;
import com.petmatch.repository.PlacementRepository;
import com.petmatch.service.ApplicationService;
import com.petmatch.service.FosterFamilyService;
import com.petmatch.service.PetService;
import com.petmatch.service.PlacementService;
import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ServiceLogicTest {
    Pet pet = new Dog(1, "Buddy", "Mix", 12, "GOOD", "CALM", false, "ANY", PetStatus.AVAILABLE, null);
    FosterFamily family = new FosterFamily(
            1,
            "Family",
            "Contact",
            "0900000000",
            HousingType.HOUSE,
            ExperienceLevel.EXPERIENCED,
            2,
            0,
            true,
            false,
            true);
    ApplicationStatus state = ApplicationStatus.PENDING;
    boolean duplicate = false;
    int writes = 0;

    @SuppressWarnings("unchecked")
    <T> T repo(Class<T> type) {
        return (T) Proxy.newProxyInstance(
                type.getClassLoader(),
                new Class<?>[] { type },
                (o, m, a) -> {
                    if (m.getName().equals("findById"))
                        return type == PetRepository.class
                                ? Optional.of(pet)
                                : type == FosterFamilyRepository.class
                                        ? Optional.of(family)
                                        : Optional.of(app());
                    if (m.getName().equals("findAll"))
                        return duplicate ? List.of(app()) : List.of();
                    if (m.getName().equals("save")) {
                        writes++;
                        return type == PetRepository.class
                                ? pet
                                : type == FosterFamilyRepository.class ? family : app();
                    }
                    if (m.getName().equals("update")
                            || m.getName().equals("delete")
                            || m.getName().equals("createPlacement")) {
                        writes++;
                        return null;
                    }
                    throw new UnsupportedOperationException(m.getName());
                });
    }

    AdoptionApplication app() {
        return new AdoptionApplication(1, pet, family, state, LocalDate.now());
    }

    ApplicationService apps() {
        // Khớp với constructor của ApplicationService.java chỉ nhận vào 1 tham số
        // ApplicationRepository
        return new ApplicationService(repo(ApplicationRepository.class));
    }

    PlacementService placements() {
        return new PlacementService(repo(PlacementRepository.class), repo(ApplicationRepository.class));
    }

    @Test
    void validApplicationWritesOnce() {
        assertNotNull(apps().createApplication(pet, family));
        assertEquals(1, writes);
    }

    @Test
    void duplicateApplicationRejected() {
        duplicate = true;
        assertNotNull(
                assertThrows(RuntimeException.class, () -> apps().createApplication(pet, family)));
        assertEquals(0, writes);
    }

    @Test
    void unavailablePetCannotApply() {
        pet.setStatus(PetStatus.PLACED);
        assertNotNull(
                assertThrows(RuntimeException.class, () -> apps().createApplication(pet, family)));
        assertEquals(0, writes);
    }

    @Test
    void rejectedApplicationCannotPlace() {
        state = ApplicationStatus.REJECTED;
        assertNotNull(
                assertThrows(RuntimeException.class, () -> placements().createPlacement(1, pet, family)));
        assertEquals(0, writes);
    }

    @Test
    void validPlacementWritesOnce() {
        placements().createPlacement(1, pet, family);
        assertEquals(1, writes);
    }

    @Test
    void placedPetCannotPlaceAgain() {
        pet.setStatus(PetStatus.PLACED);
        assertNotNull(
                assertThrows(RuntimeException.class, () -> placements().createPlacement(1, pet, family)));
        assertEquals(0, writes);
    }

    @Test
    void negativeAgeDoesNotWrite() {
        pet.setAgeMonths(-1);
        assertNotNull(
                assertThrows(
                        RuntimeException.class, () -> new PetService(repo(PetRepository.class)).save(pet)));
        assertEquals(0, writes);
    }

    @Test
    void blankNameDoesNotWrite() {
        pet.setName("");
        assertNotNull(
                assertThrows(
                        RuntimeException.class, () -> new PetService(repo(PetRepository.class)).save(pet)));
        assertEquals(0, writes);
    }

    @Test
    void existingPetUsesUpdate() {
        assertSame(pet, new PetService(repo(PetRepository.class)).save(pet));
        assertEquals(1, writes);
    }

    @Test
    void invalidHousingDoesNotWrite() {
        pet.setHousingRequirement("BOAT");
        assertNotNull(
                assertThrows(
                        RuntimeException.class, () -> new PetService(repo(PetRepository.class)).save(pet)));
        assertEquals(0, writes);
    }

    @Test
    void invalidFamilyDoesNotWrite() {
        var bad = new FosterFamily(
                0,
                "Family",
                "",
                "x",
                HousingType.HOUSE,
                ExperienceLevel.BEGINNER,
                0,
                -1,
                false,
                false,
                true);
        assertNotNull(
                assertThrows(
                        RuntimeException.class,
                        () -> new FosterFamilyService(repo(FosterFamilyRepository.class)).save(bad)));
        assertEquals(0, writes);
    }

    @Test
    void finalApplicationCannotBeApproved() {
        state = ApplicationStatus.PLACED;
        assertThrows(
                RuntimeException.class,
                () -> apps().updateApplicationStatus(1, ApplicationStatus.APPROVED));
        assertEquals(0, writes);
    }

    @Test
    void callerCannotSetPlacedDirectly() {
        assertThrows(
                RuntimeException.class, () -> apps().updateApplicationStatus(1, ApplicationStatus.PLACED));
        assertEquals(0, writes);
    }

    @Test
    void inactiveFamilyCannotApply() {
        family.setActive(false);
        assertThrows(RuntimeException.class, () -> apps().createApplication(pet, family));
        assertEquals(0, writes);
    }

    @Test
    void committedFamilyCannotPlaceDespiteSpareCapacity() {
        family.setActivePetCount(1);
        assertThrows(RuntimeException.class, () -> placements().createPlacement(1, pet, family));
        assertEquals(0, writes);
    }

    @Test
    void failedStrictMatchCannotBeApproved() {
        pet.setSpecialCareRequired(true);
        family.setExperienceLevel(ExperienceLevel.BEGINNER);
        assertThrows(
                RuntimeException.class,
                () -> apps().updateApplicationStatus(1, ApplicationStatus.APPROVED));
        assertEquals(0, writes);
    }
}
