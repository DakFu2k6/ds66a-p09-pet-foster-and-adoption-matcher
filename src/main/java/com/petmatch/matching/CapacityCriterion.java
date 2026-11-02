package main.java.com.petmatch.matching;

import main.java.com.petmatch.model.*;

public class CapacityCriterion implements MatchCriterion {
    public String getName() {
        return "Available capacity";
    }

    public CriterionResult evaluate(Pet pet, FosterFamily family) {
        boolean pass = family.hasCapacity();
        return new CriterionResult(getName(), pass, pass
                ? "The family has available capacity."
                : "The family is inactive or has reached its capacity.");
    }
}
