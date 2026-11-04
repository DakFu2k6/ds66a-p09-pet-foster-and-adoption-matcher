package com.petmatch;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.petmatch.matching.CriterionResult;
import com.petmatch.model.Dog;
import com.petmatch.model.ExperienceLevel;
import com.petmatch.model.FosterFamily;
import com.petmatch.model.HousingType;
import com.petmatch.model.MatchOperator;
import com.petmatch.model.Pet;
import com.petmatch.model.PetStatus;
import com.petmatch.service.MatchingService;
import org.junit.jupiter.api.Test;

class MatchingServiceTest {

    @Test
    void andRequiresEveryCriterionToPass() {
        MatchingService service = new MatchingService();
        Pet pet = new Dog(1, "Buddy", "Mix", 12, "GOOD", "CALM", true, "HOUSE", PetStatus.AVAILABLE, null);
        FosterFamily family = new FosterFamily(
                1,
                "Family",
                "A",
                "090",
                HousingType.HOUSE,
                ExperienceLevel.BEGINNER,
                2,
                0,
                true,
                false,
                true);

        CriterionResult result = service.evaluateMatch(pet, family, MatchOperator.AND);

        assertNotNull(result);
        assertFalse(result.isSatisfied());
    }
}
