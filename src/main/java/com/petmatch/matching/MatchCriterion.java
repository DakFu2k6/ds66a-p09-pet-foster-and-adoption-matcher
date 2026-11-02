package com.petmatch.matching;

import com.petmatch.model.FosterFamily;
import com.petmatch.model.Pet;

public interface MatchCriterion {
    String getName();

    CriterionResult evaluate(Pet pet, FosterFamily family);
}
