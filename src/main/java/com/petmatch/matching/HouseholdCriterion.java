package com.petmatch.matching;

import com.petmatch.model.*;

public class HouseholdCriterion implements MatchCriterion {
    public CriterionResult evaluate(Pet pet, FosterFamily family) {
        if (pet == null || family == null)
            return CriterionResult.fail("Household: missing data");
        boolean ok = !family.isHasChildren()
                || !java.util.Set.of("AGGRESSIVE", "SHY").contains(pet.getTemperament());
        return new CriterionResult(
                ok,
                "Household: temperament=" + pet.getTemperament() + ", children=" + family.isHasChildren());
    }
}
