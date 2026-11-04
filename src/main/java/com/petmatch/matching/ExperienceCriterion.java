package com.petmatch.matching;

import com.petmatch.model.*;

public class ExperienceCriterion implements MatchCriterion {
    public CriterionResult evaluate(Pet pet, FosterFamily family) {
        if (pet == null || family == null || family.getExperienceLevel() == null)
            return CriterionResult.fail("Experience: missing data");
        var required = pet.requiredExperience();
        return new CriterionResult(
                family.getExperienceLevel().ordinal() >= required.ordinal(),
                "Experience: requires "
                        + required
                        + "; family "
                        + family.getExperienceLevel()
                        + ". "
                        + pet.getCareDescription());
    }
}
