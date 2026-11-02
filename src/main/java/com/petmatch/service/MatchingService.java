package com.petmatch.service;

import com.petmatch.matching.*;
import com.petmatch.model.*;
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
        if (results == null || results.isEmpty() || operator == null)
            return false;
        if (operator == MatchOperator.AND)
            return results.stream().allMatch(CriterionResult::passed);
        return results.stream().anyMatch(CriterionResult::passed);
    }
}
