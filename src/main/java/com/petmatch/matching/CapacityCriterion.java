package com.petmatch.matching;

import com.petmatch.model.FosterFamily;
import com.petmatch.model.Pet;

public class CapacityCriterion implements MatchCriterion {
    @Override
    public CriterionResult evaluate(Pet pet, FosterFamily family) {
        boolean ok = family != null && family.hasCapacity();
        return new CriterionResult(
                ok,
                "Capacity: "
                        + (family == null
                                ? "missing family"
                                : family.getActivePetCount() + " / " + family.getCapacity() + " occupied"));
    }
}
