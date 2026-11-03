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
        if (pet == null || family == null) {
            throw new IllegalArgumentException("Thú cưng và gia đình nhận nuôi không được để trống khi đánh giá ghép nối.");
        }

        MatchOperator op = (operator != null) ? operator : MatchOperator.AND;
        
        CriterionGroup group = new CriterionGroup(op);
        
        group.addCriterion(new CapacityCriterion());
        group.addCriterion(new HousingCriterion());
        group.addCriterion(new ExperienceCriterion());

        return group.evaluate(pet, family);
    }
}