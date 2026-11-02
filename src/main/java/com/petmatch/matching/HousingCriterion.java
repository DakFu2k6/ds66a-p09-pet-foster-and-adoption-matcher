package main.java.com.petmatch.matching;

import main.java.com.petmatch.model.*;

public class HousingCriterion implements MatchCriterion {
    public String getName() {
        return "Housing compatibility";
    }

    public CriterionResult evaluate(Pet pet, FosterFamily family) {
        String requirement = pet.getHousingRequirement();
        boolean pass = "ANY".equalsIgnoreCase(requirement)
                || ("HOUSE".equalsIgnoreCase(requirement) && family.getHousingType() != HousingType.APARTMENT)
                || ("APARTMENT".equalsIgnoreCase(requirement) && family.getHousingType() == HousingType.APARTMENT)
                || ("YARD".equalsIgnoreCase(requirement) && family.isHasYard());
        return new CriterionResult(getName(), pass, pass
                ? "The family's housing meets the pet's requirement."
                : "The family's housing does not meet the pet's requirement (" + requirement + ").");
    }
}
