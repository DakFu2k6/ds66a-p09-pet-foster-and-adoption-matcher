package com.petmatch;

import com.petmatch.model.*;
import com.petmatch.service.MatchingService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MatchingServiceTest {
    @Test
    void andRequiresEveryCriterionToPass() {
        MatchingService service = new MatchingService();
        Pet pet = new Dog(1, "Buddy", "Mix", 12, "GOOD", "CALM", true, "HOUSE", PetStatus.AVAILABLE, null);
        FosterFamily family = new FosterFamily(1, "Family", "A", "090", HousingType.HOUSE, ExperienceLevel.BEGINNER, 2,
                0, true, false, true);
        var results = service.evaluate(pet, family);
        assertFalse(service.isPassed(results, MatchOperator.AND));
        assertTrue(service.isPassed(results, MatchOperator.OR));
    }
}
