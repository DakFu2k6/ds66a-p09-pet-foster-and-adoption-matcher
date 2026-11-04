package com.petmatch.matching;

import com.petmatch.model.FosterFamily;
import com.petmatch.model.Pet;

public class ExperienceCriterion implements MatchCriterion {
    @Override
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
