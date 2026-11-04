package com.petmatch.service;

import com.petmatch.matching.*;
import com.petmatch.model.*;

public class MatchingService {
    private final java.util.List<MatchCriterion> careRules;

    public MatchingService() {
        this(
                java.util.List.of(
                        new HousingCriterion(), new ExperienceCriterion(), new HouseholdCriterion()));
    }

    public MatchingService(java.util.List<MatchCriterion> rules) {
        careRules = java.util.List.copyOf(rules);
        if (careRules.isEmpty())
            throw new IllegalArgumentException("Criteria cannot be empty");
    }

    public CriterionResult evaluateMatch(Pet pet, FosterFamily family, MatchOperator operator) {
        if (pet == null || family == null)
            throw new IllegalArgumentException("Pet and family are required.");
        CriterionGroup rules = new CriterionGroup(operator == null ? MatchOperator.AND : operator);
        careRules.forEach(rules::addCriterion);
        CriterionGroup mandatory = new CriterionGroup(MatchOperator.AND);
        mandatory.addCriterion(new CapacityCriterion());
        mandatory.addCriterion(
                (p, f) -> new CriterionResult(
                        p.getStatus() == PetStatus.AVAILABLE, "Pet must be AVAILABLE: " + p.getStatus()));
        mandatory.addCriterion(
                (p, f) -> new CriterionResult(
                        f.isActive() && f.getActivePetCount() == 0,
                        "Family must be active and uncommitted (occupied=" + f.getActivePetCount() + ")"));
        mandatory.addCriterion(rules);
        return mandatory.evaluate(pet, family);
    }
}
