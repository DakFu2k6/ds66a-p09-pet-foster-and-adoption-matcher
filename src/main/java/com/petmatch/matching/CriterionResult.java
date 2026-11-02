package com.petmatch.matching;

public class CriterionResult {
    private final boolean satisfied; 
    private final String reason;     

    public CriterionResult(boolean satisfied, String reason) {
        this.satisfied = satisfied;
        this.reason = reason;
    }

    public static CriterionResult pass(String reason) {
        return new CriterionResult(true, reason);
    }

    public static CriterionResult fail(String reason) {
        return new CriterionResult(false, reason);
    }

    public boolean isSatisfied() {
        return satisfied;
    }

    public String getReason() {
        return reason;
    }
}