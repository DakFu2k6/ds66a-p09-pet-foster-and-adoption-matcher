package com.petmatch.matching;

import com.petmatch.model.FosterFamily;
import com.petmatch.model.Pet;

public interface MatchCriterion {
    CriterionResult evaluate(Pet pet, FosterFamily family);
}

