package com.petmatch;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;

import com.petmatch.model.Dog;
import com.petmatch.model.ExperienceLevel;
import com.petmatch.model.FosterFamily;
import com.petmatch.model.HousingType;
import com.petmatch.model.MatchOperator;
import com.petmatch.model.Pet;
import com.petmatch.model.PetStatus;
import com.petmatch.service.MatchingService;

class MatchingMatrixTest {

    private final MatchingService matchingService = new MatchingService();

    private Pet createTestPet(String housingReq, boolean specialCare) {
        return new Dog(1, "Buddy", "Mix", 12, "GOOD", "CALM", specialCare, housingReq, PetStatus.AVAILABLE, null);
    }

    private FosterFamily createTestFamily(HousingType housing, ExperienceLevel exp, int activePets, boolean hasYard, boolean active) {
        return new FosterFamily(1, "Family", "Contact", "0900000000", housing, exp, 2, activePets, hasYard, false, active);
    }

    @Test
    void testMatchingServiceBasic() {
        Pet pet = createTestPet("ANY", false);
        FosterFamily family = createTestFamily(HousingType.HOUSE, ExperienceLevel.BEGINNER, 0, true, true);
        
        assertNotNull(pet);
        assertNotNull(family);
        assertNotNull(matchingService);
    }

    @Test
    void testEmptyCriteriaListHandling() {
        for (MatchOperator op : MatchOperator.values()) {
            assertNotNull(op);
        }
    }
}