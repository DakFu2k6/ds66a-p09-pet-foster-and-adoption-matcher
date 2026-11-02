package com.petmatch.service;

import com.petmatch.matching.CapacityCriterion;
import com.petmatch.matching.CriterionGroup;
import com.petmatch.matching.CriterionResult;
import com.petmatch.matching.ExperienceCriterion;
import com.petmatch.matching.HousingCriterion;
import com.petmatch.model.FosterFamily;
import com.petmatch.model.MatchOperator;
import com.petmatch.model.Pet;

public class MatchingService {

    public CriterionResult evaluateMatch(Pet pet, FosterFamily family, MatchOperator operator) {
        CriterionGroup group = new CriterionGroup(operator);
        
        group.addCriterion(new CapacityCriterion());
        group.addCriterion(new HousingCriterion());
        group.addCriterion(new ExperienceCriterion());

        return group.evaluate(pet, family);
    }
}