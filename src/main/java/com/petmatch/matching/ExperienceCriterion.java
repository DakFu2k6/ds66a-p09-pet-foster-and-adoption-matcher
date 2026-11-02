package com.petmatch.matching;

import com.petmatch.model.*;

public class ExperienceCriterion implements MatchCriterion {
    public String getName() {
        return "Care experience";
    }

    public CriterionResult evaluate(Pet pet, FosterFamily family) {
        boolean pass = !pet.isSpecialCareRequired() || family.getExperienceLevel() != ExperienceLevel.BEGINNER;
        return new CriterionResult(getName(), pass, pass
                ? "The family's experience level is acceptable."
                : "Special-care pets require at least intermediate experience.");
    }
}
