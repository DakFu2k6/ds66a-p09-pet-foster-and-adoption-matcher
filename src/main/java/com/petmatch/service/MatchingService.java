package main.java.com.petmatch.service;

import main.java.com.petmatch.matching.*;
import main.java.com.petmatch.model.*;
import java.util.*;

public class MatchingService {
    private final CriterionGroup group;

    public MatchingService() {
        group = new CriterionGroup(List.of(new HousingCriterion(), new ExperienceCriterion(), new CapacityCriterion()),
                MatchOperator.AND);
    }

    public List<CriterionResult> evaluate(Pet pet, FosterFamily family) {
        return group.evaluate(pet, family);
    }

    public boolean isPassed(List<CriterionResult> results, MatchOperator operator) {
        if (operator == MatchOperator.AND)
            return results.stream().allMatch(CriterionResult::passed);
        return results.stream().anyMatch(CriterionResult::passed);
    }
}
