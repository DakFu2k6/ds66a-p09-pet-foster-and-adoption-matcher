package com.petmatch;

import org.junit.jupiter.api.Test;
import com.petmatch.model.*;
import com.petmatch.matching.*;
import com.petmatch.service.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;

class MatchingMatrixTest {
    Pet pet(String house, boolean special) {
        return new Dog(1, "Buddy", "Mix", 12, "GOOD", "CALM", special, house, PetStatus.AVAILABLE, null);
    }

    FosterFamily family(HousingType h, ExperienceLevel e, int count, boolean yard, boolean active) {
        return new FosterFamily(1, "Family", "Contact", "0900000000", h, e, 2, count, yard, false, active);
    }

    @Test
    void housingMatrix() {
        for (String requirement : List.of("ANY", "HOUSE", "APARTMENT", "YARD"))
            for (HousingType h : HousingType.values())
                for (boolean yard : List.of(true, false)) {
                    boolean expected = requirement.equals("ANY")
                            || requirement.equals("HOUSE") && h != HousingType.APARTMENT
                            || requirement.equals("APARTMENT") && h == HousingType.APARTMENT
                            || requirement.equals("YARD") && yard;
                    assertEquals(expected, new HousingCriterion()
                            .evaluate(pet(requirement, false), family(h, ExperienceLevel.BEGINNER, 0, yard, true))
                            .passed());
                }
    }

    @Test
    void experienceMatrix() {
        for (boolean special : List.of(true, false))
            for (ExperienceLevel e : ExperienceLevel.values())
                assertEquals(!special || e != ExperienceLevel.BEGINNER, new ExperienceCriterion()
                        .evaluate(pet("ANY", special), family(HousingType.HOUSE, e, 0, true, true)).passed());
    }

    @Test
    void capacityBoundaries() {
        for (int count : List.of(0, 1, 2))
            for (boolean active : List.of(true, false))
                assertEquals(active && count < 2,
                        new CapacityCriterion()
                                .evaluate(pet("ANY", false),
                                        family(HousingType.HOUSE, ExperienceLevel.EXPERIENCED, count, true, active))
                                .passed());
    }

    @Test
    void emptyResultsCannotApprove() {
        for (MatchOperator o : MatchOperator.values())
            assertFalse(new MatchingService().isPassed(List.of(), o));
    }

    @Test
    void groupMustHaveCriteria() {
        assertThrows(IllegalArgumentException.class, () -> new CriterionGroup(List.of(), MatchOperator.AND));
    }
}
