package com.petmatch.matching;

import java.util.ArrayList;
import java.util.List;

import com.petmatch.model.FosterFamily;
import com.petmatch.model.MatchOperator;
import com.petmatch.model.Pet;

public class CriterionGroup implements MatchCriterion {
    private final List<MatchCriterion> criteria = new ArrayList<>();
    private MatchOperator operator = MatchOperator.AND;

    public CriterionGroup() {}

    public CriterionGroup(MatchOperator operator) {
        this.operator = operator;
    }

    public void addCriterion(MatchCriterion criterion) {
        this.criteria.add(criterion);
    }

    public void setOperator(MatchOperator operator) {
        this.operator = operator;
    }

    @Override
    public CriterionResult evaluate(Pet pet, FosterFamily family) {
        if (criteria.isEmpty()) {
            return CriterionResult.pass("Không có tiêu chí nào được thiết lập.");
        }

        StringBuilder details = new StringBuilder();
        boolean overallResult = (operator == MatchOperator.AND);

        for (MatchCriterion criterion : criteria) {
            CriterionResult result = criterion.evaluate(pet, family);
            details.append("- ").append(result.getReason()).append("\n");

            if (operator == MatchOperator.AND) {
                if (!result.isSatisfied()) {
                    overallResult = false;
                }
            } else if (operator == MatchOperator.OR) {
                if (result.isSatisfied()) {
                    overallResult = true;
                }
            }
        }

        if (overallResult) {
            return CriterionResult.pass("ĐÁNH GIÁ CHUNG: ĐẠT (Logic " + operator + ")\n" + details.toString());
        } else {
            return CriterionResult.fail("ĐÁNH GIÁ CHUNG: KHÔNG ĐẠT (Logic " + operator + ")\n" + details.toString());
        }
    }
}