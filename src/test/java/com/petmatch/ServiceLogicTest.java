package com.petmatch;

import org.junit.jupiter.api.Test;
import com.petmatch.model.*;
import com.petmatch.repository.*;
import com.petmatch.service.*;
import java.lang.reflect.Proxy;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class ServiceLogicTest {
    Pet pet = new Dog(1, "Buddy", "Mix", 12, "GOOD", "CALM", false, "ANY", PetStatus.AVAILABLE, null);
    FosterFamily family = new FosterFamily(1, "Family", "Contact", "0900000000", HousingType.HOUSE,
            ExperienceLevel.EXPERIENCED, 2, 0, true, false, true);
    ApplicationStatus state = ApplicationStatus.PENDING;
    boolean duplicate = false;
    int writes = 0;

    @SuppressWarnings("unchecked")
    <T> T repo(Class<T> type) {
        return (T) Proxy.newProxyInstance(type.getClassLoader(), new Class<?>[] { type }, (o, m, a) -> {
            if (m.getName().equals("findById"))
                return type == PetRepository.class ? Optional.of(pet)
                        : type == FosterFamilyRepository.class ? Optional.of(family) : Optional.of(app());
            if (m.getName().equals("findAll"))
                return duplicate ? List.of(app()) : List.of();
            if (m.getName().equals("save")) {
                writes++;
                return type == PetRepository.class ? pet : type == FosterFamilyRepository.class ? family : app();
            }
            if (m.getName().equals("update") || m.getName().equals("delete")) {
                writes++;
                return null;
            }
            throw new UnsupportedOperationException(m.getName());
        });
    }

    AdoptionApplication app() {
        return new AdoptionApplication(1, 1, 1, LocalDateTime.now(), state, "");
    }

    ApplicationService apps() {
        return new ApplicationService(repo(ApplicationRepository.class), repo(PetRepository.class),
                repo(FosterFamilyRepository.class));
    }

    PlacementService placements() {
        return new PlacementService((a, p, f) -> writes++, repo(ApplicationRepository.class), repo(PetRepository.class),
                repo(FosterFamilyRepository.class), new MatchingService());
    }

    @Test
    void validApplicationWritesOnce() {
        assertNotNull(apps().create(1, 1, ""));
        assertEquals(1, writes);
    }

    @Test
    void duplicateApplicationRejected() {
        duplicate = true;
        assertThrows(RuntimeException.class, () -> apps().create(1, 1, ""));
        assertEquals(0, writes);
    }

    @Test
    void unavailablePetCannotApply() {
        pet.setStatus(PetStatus.PLACED);
        assertThrows(RuntimeException.class, () -> apps().create(1, 1, ""));
        assertEquals(0, writes);
    }

    @Test
    void rejectedApplicationCannotPlace() {
        state = ApplicationStatus.REJECTED;
        assertThrows(RuntimeException.class, () -> placements().confirm(1));
        assertEquals(0, writes);
    }

    @Test
    void validPlacementWritesOnce() {
        placements().confirm(1);
        assertEquals(1, writes);
    }

    @Test
    void placedPetCannotPlaceAgain() {
        pet.setStatus(PetStatus.PLACED);
        assertThrows(RuntimeException.class, () -> placements().confirm(1));
        assertEquals(0, writes);
    }

    @Test
    void negativeAgeDoesNotWrite() {
        pet.setAgeMonths(-1);
        assertThrows(RuntimeException.class, () -> new PetService(repo(PetRepository.class)).save(pet));
        assertEquals(0, writes);
    }

    @Test
    void blankNameDoesNotWrite() {
        pet.setName(" ");
        assertThrows(RuntimeException.class, () -> new PetService(repo(PetRepository.class)).save(pet));
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
        assertThrows(RuntimeException.class, () -> new PetService(repo(PetRepository.class)).save(pet));
        assertEquals(0, writes);
    }

    @Test
    void invalidFamilyDoesNotWrite() {
        var bad = new FosterFamily(0, "Family", "", "x", HousingType.HOUSE, ExperienceLevel.BEGINNER, 0, -1, false,
                false, true);
        assertThrows(RuntimeException.class,
                () -> new FosterFamilyService(repo(FosterFamilyRepository.class)).save(bad));
        assertEquals(0, writes);
    }
}
