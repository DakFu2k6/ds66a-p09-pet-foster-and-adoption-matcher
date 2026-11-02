package com.petmatch.matching;

import com.petmatch.model.Pet;
import com.petmatch.model.FosterFamily;

public interface MatchCriterion {
    String getName();

    CriterionResult evaluate(Pet pet, FosterFamily family);
}
