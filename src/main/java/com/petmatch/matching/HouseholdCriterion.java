package com.petmatch.matching;

import com.petmatch.model.FosterFamily;
import com.petmatch.model.Pet;

public class HouseholdCriterion implements MatchCriterion {
    @Override
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
