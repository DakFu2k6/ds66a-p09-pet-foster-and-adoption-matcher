package com.petmatch.matching;

import com.petmatch.model.*;
import java.util.*;

public class CriterionGroup {
    private final List<MatchCriterion> criteria;
    private final MatchOperator operator;

    public CriterionGroup(List<MatchCriterion> criteria, MatchOperator operator) {
        this.criteria = List.copyOf(criteria);
        this.operator = operator;
        if (criteria.isEmpty())
            throw new IllegalArgumentException("At least one criterion is required.");
    }

    public List<CriterionResult> evaluate(Pet pet, FosterFamily family) {
        return criteria.stream().map(c -> c.evaluate(pet, family)).toList();
    }

    public boolean isPassed(List<CriterionResult> results) {
        return operator == MatchOperator.AND ? results.stream().allMatch(CriterionResult::passed)
                : results.stream().anyMatch(CriterionResult::passed);
    }

    public MatchOperator getOperator() {
        return operator;
    }
}
