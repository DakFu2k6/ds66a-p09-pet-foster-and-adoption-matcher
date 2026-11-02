package main.java.com.petmatch.matching;

import main.java.com.petmatch.model.Pet;
import main.java.com.petmatch.model.FosterFamily;

public interface MatchCriterion {
    String getName();

    CriterionResult evaluate(Pet pet, FosterFamily family);
}
