package com.petmatch.matching;

import com.petmatch.model.*;

public class CapacityCriterion implements MatchCriterion {
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
