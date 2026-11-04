package com.petmatch.matching;

import com.petmatch.model.FosterFamily;
import com.petmatch.model.Pet;

public class HousingCriterion implements MatchCriterion {
    @Override
    public CriterionResult evaluate(Pet pet, FosterFamily family) {
        if (pet == null || family == null || family.getHousingType() == null)
            return CriterionResult.fail("Housing: missing data");
        boolean ok = switch (pet.getHousingRequirement() == null ? "" : pet.getHousingRequirement()) {
            case "ANY" -> true;
            case "HOUSE" ->
                family.getHousingType() == com.petmatch.model.HousingType.HOUSE
                        || family.getHousingType() == com.petmatch.model.HousingType.FARM;
            case "APARTMENT" -> family.getHousingType() == com.petmatch.model.HousingType.APARTMENT;
            case "YARD" -> family.isHasYard();
            default -> false;
        };
        return new CriterionResult(
                ok,
                "Housing: requires "
                        + pet.getHousingRequirement()
                        + "; family "
                        + family.getHousingType()
                        + ", yard="
                        + family.isHasYard());
    }
}
